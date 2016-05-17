# JD-TccTx
分布式事务管理框架，实现基于Try Confirm Cancel的分布式事务调度。
目前提供了同步事务的自动调度能力，并能进行回滚和失败任务的异步重试流程。

## How to build

编译Druid Notice需要如下资源:

* Latest stable [Oracle JDK 7](http://www.oracle.com/technetwork/java/)
* Latest stable [Apache Maven](http://maven.apache.org/)

下载代码及编译方法
```
git clone git@git.jd.com:pop-commons/jd-tcctx.git
cd jd-tcctx
mvn -DskipTests=true package
```
 
## Features

*   基于业务系统自身的事务主表进行事务驱动（需配置额外的状态和处理时间字段）
*   子事务资源只需提供try, confirm, cancel三个方法(try和cancel可被裁减)，就能自动进行事务驱动
*   通过异常自动控制事务流程进入异步重试还是回滚流程
*   提供积压任务监控页面，方便查看当前任务状态

## Development guide

#### 1. 增加Maven依赖

在项目pom.xml增加依赖
```xml
<project>
    ...
    <dependencies>
        ...
        <!-- tcctx核心依赖 -->
        <dependency>
            <groupId>com.jd.tx</groupId>
            <artifactId>tcctx-core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        
        <!-- 提供监控servlet，如果不需要可以不配置 -->
        <dependency>
            <groupId>com.jd.tx</groupId>
            <artifactId>tcctx-console</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
```

#### 2. 包装实现业务方法

将事务流程分离为若干能**独立保持原子性、强一致性和幂等性**的子事务单元。
每个子事务单元实现接口[ResourceItem](http://git.jd.com/pop-commons/jd-tcctx/blob/master/tcctx-core/src/main/java/com/jd/tx/tcc/core/ResourceItem.java)。
其中可以裁减的是`tryTx()`和`cancelTx()`方法，需要单独在`hasTry()`或`hasCancel()`中`return false;`。

-------------------------------------------------------------------------------

需要注意的是在try, confirm, cancel的业务方法中，如果出现异常，可以选择两种抛出方式：
* [SOATxUnawareException](http://git.jd.com/pop-commons/jd-tcctx/blob/master/tcctx-core/src/main/java/com/jd/tx/tcc/core/exception/SOATxUnawareException.java)
* [SOATxUnrecoverableException](http://git.jd.com/pop-commons/jd-tcctx/blob/master/tcctx-core/src/main/java/com/jd/tx/tcc/core/exception/SOATxUnrecoverableException.java)

> 当抛出`SOATxUnawareException`或其他继承自`Throwable`的异常后，事务流程认为发生了不可预期的异常（例如网络超时等），当前事务线程会中断并直接抛出该异常，但是保留当前事务，后续会有异步任务自动根据当前状态进行重试。
> 而当抛出`SOATxUnrecoverableException`后，事务流程认为发生了预期进行回滚的异常（例如资源预占失败，数据库主键重复等可预期的异常），事务流程会依次调用当前节点直至最初节点的`cancelTx()`方法，直到全部回滚成功并抛出该异常。

**也就是说tcctx是以抛出异常的类型进行事务流程驱动的**

#### 3. 在spring中配置TCC事务

##### 3.1 配置事务主表和子事务相关

样例：
```xml
    <bean id="points2CouponTxRes" class="com.jd.tx.tcc.core.TransactionResource" init-method="init">
        <!-- 事务主表表名 -->
        <property name="table" value="crm_cust_p_freeze"/>
        <!-- 主键，必须唯一 -->
        <property name="idCol" value="business_id"/>
        <!-- 事务状态 -->
        <property name="stateCol" value="status"/>
        <!-- 事务最后一次执行日志，用于失败重试任务的监控，可以没有此字段 -->
        <property name="msgCol" value="process_msg"/>
        <!-- 当存在msgCol字段时，能够存储msg的最大长度 -->
        <property name="msgMaxLength" value="500"/>
        <!-- 最后一次执行时间，用于计算是否超时任务 -->
        <property name="handleTimeCol" value="last_handle_time"/>
        <!-- 事务状态码生成器，可以自己实现或者通过代码生成 -->
        <property name="stateGenerator" ref="seqStateGenerator" />
        <property name="resourceItems">
            <list>
                <!-- 注意：次序为事务执行次序，具体实现为ResourceItem的实现类 -->
                <ref bean="sendCouponAction" />
                <ref bean="decreasePointsAction" />
            </list>
        </property>
    </bean>
    
    <!-- 默认提供的顺序状态码生成器 -->
    <bean id="seqStateGenerator" class="com.jd.tx.tcc.core.impl.SeqStateGenerator" />
```

##### 3.2 配置事务资源管理器

样例：
```xml
    <!-- 事务资源管理器 -->
    <bean id="transactionManager" class="com.jd.tx.tcc.core.TransactionManager">
        <property name="resourcesMap">
            <map>
                <!-- 注册上一步配置的事务定义，并给予唯一key主键，如果一个容器内有多个TCC事务，需要定义不同的实现并用key区分 -->
                <entry key="points2Coupon" value-ref="points2CouponTxRes"/>
            </map>
        </property>
    </bean>
```

##### 3.3 配置提交执行器

目前默认只提供了一种实现：
```xml
    <bean id="transactionRunner" class="com.jd.tx.tcc.core.sync.SyncTransactionRunner"/>
```

#### 4. 在代码中提交TCC事务

首先获得spring context中的`TransactionRunner`
```java
@Autowired
private TransactionRunner transactionRunner;
```
构建TransactionContext，需要传入事务主表所需的`DataSource`和本次事务主对象txObject
```java
    TransactionContext context = TXContextFactory.buildNew(dataSource, txObject);
...
    //此处的key与事务资源管理器中的key相对应
    public static final String CONTEXT_KEY = "points2Coupon";

    public static CommonTransactionContext<CustomerPointsFreeze> buildNew(
            DataSource dataSource,
            CustomerPointsFreeze pointsFreeze) {
        CommonTransactionContext context = new CommonTransactionContext();
        context.setKey(CONTEXT_KEY);
        context.setDataSource(dataSource);
        context.setId(pointsFreeze.getBusinessId());
        context.setResourceObject(pointsFreeze);
        //新提交的状态都为begin
        context.setState(transactionResource.getBeginningState());
        return context;
    }
```
提交事务，如果抛出`SOATxUnawareException`表示事务进入异步重试流程。如果抛出`SOATxUnrecoverableException`表示事务失败，没有提交成功或者全部回滚完成。
```java
transactionRunner.run(context);
```
#### 5. 异步重试任务的配置

##### 5.1 使用Executor Scheduler进行异步任务的驱动

初始化`com.jd.tx.tcc.core.retry.RetryJob`，并调用`start()`方法，会根据配置自动进行超时任务的重试。此种方式较为轻量级，但是不具备分片，HA，Failover能力。

##### 5.2 使用elastic-job进行异步任务的驱动

这种任务基于elastic-job进行调度，相对重量级，但是通过elastic-job实现了分片，Failover能力。但是需要单独部署一套elastic-job。
引入tcctx-job依赖：
```xml
<project>
    ...
    <dependencies>
        ...
        <!-- tcctx 重试调度任务依赖 -->
        <dependency>
            <groupId>com.jd.tx</groupId>
            <artifactId>tcctx-job</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
```
样例：
```xml
    <!-- 失败积分换券重处理作业-->
    <bean id="point2CouponRetryJob" class="com.jd.tx.tcc.job.SyncJobRetryScheduler">
        <property name="transactionRunner" ref="transactionRunner" />
        <property name="dataSourceMap" ref="dataSourceMap" />
        <property name="dbPrefix" value="db" />
    </bean>

    <!-- 失败积分换券重处理任务-->
    <bean id="point2CouponRetryScheduler" class="com.dangdang.ddframe.job.spring.schedule.SpringJobScheduler" init-method="init">
        <constructor-arg ref="regCenter" />
        <constructor-arg>
            <bean class="com.dangdang.ddframe.job.api.JobConfiguration">
                <constructor-arg index="0" type="java.lang.String" value="tradePointsRetryScheduler" />
                <constructor-arg index="1" value="com.jd.tx.tcc.job.SyncJobRetryScheduler" />
                <constructor-arg index="2" type="int" value="3" />
                <constructor-arg index="3" type="java.lang.String" value="0 0/1 * * * ?" />
                <property name="shardingItemParameters" value="0=0,1=1,2=2" />
                <!-- the key in soa tx -->
                <property name="jobParameter" value="{key: 'points2Coupon', dataSource: '2'}" />
                <property name="overwrite" value="true" />
                <property name="disabled" value="false" />
            </bean>
        </constructor-arg>
    </bean>
```

#### 6. 积压中任务的监控

在J2EE容器的web.xml中配置servlet：
```xml
    <!--TccTx monitor page-->
    <servlet>
        <servlet-name>TccTxEntityView</servlet-name>
        <servlet-class>com.jd.tx.tcc.console.TXMonitorServlet</servlet-class>
        <init-param>
            <param-name>queryBeanName</param-name>
            <param-value>transactionQueryService</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>TccTxEntityView</servlet-name>
        <url-pattern>/tcctx/*</url-pattern>
    </servlet-mapping>
```
启动web容器后，访问`http://yoursite/tccctx/index.html`进行监控

