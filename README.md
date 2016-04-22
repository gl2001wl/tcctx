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


