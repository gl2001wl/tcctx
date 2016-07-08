package com.jd.tx.tcc.core;

import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;
import com.jd.tx.tcc.core.impl.CommonTransactionContext;
import com.jd.tx.tcc.core.impl.SeqStateGenerator;
import com.jd.tx.tcc.core.sync.SyncTransactionRunner;
import com.jd.tx.tcc.core.test.hsql.HsqlDatabase;
import com.jd.tx.tcc.core.test.hsql.TestTableManager;
import com.jd.tx.tcc.core.test.mock.MockResourceItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
@Slf4j
public class TestTransactionRunner {

    private TransactionRunner txRunner;

    private TransactionResource txRes;

    private TransactionManager txManager;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws SQLException {
        TestTableManager.createTestTables();
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        TestTableManager.dropTestTables();
    }

    @Before
    public void setup() {
        txRes = buildTxRes();
        txManager = new TransactionManager();
        Map<String, TransactionResource> resourceMap = new HashMap<>();
        resourceMap.put("TEST_TX", txRes);

        TransactionManager txManager = new TransactionManager();
        txManager.setResourcesMap(resourceMap);

        txRunner = new SyncTransactionRunner();
        txRunner.setTransactionManager(txManager);
    }

    @Test
    public void testSimpleFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyCommit());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyCommit().noTry());
        txRes.init();

        CommonTransactionContext context = buildContext("999");
        txRunner.run(context);

        verify(txRes.getResourceItems().get(0), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("999");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Assert.assertEquals("[999, "
                + txRes.getResourceItems().get(1).getStateMapping().get(ResourceItem.State.confirmSuccess)
                + ", unit test, "
                + today + "]",
                dbValue);
    }

    @Test
    public void testInterruptCommitFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().noTry().throwUnawareExWhenCommit());
        txRes.getResourceItems().add(MockResourceItem.buildMock().noTry());
        txRes.init();

        CommonTransactionContext context = buildContext("998");

//        thrown.expect(SOATxUnawareException.class);
//        thrown.expectMessage("unaware exception");
        try {
            txRunner.run(context);
            fail("No SOATxUnawareException be thrown!");
        } catch (SOATxUnawareException e) {
            log.info("got SOATxUnawareException successful!");
        }

        verify(txRes.getResourceItems().get(0), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("998");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //the state is the success state of last item, waiting for retrying.
        Assert.assertEquals("[998, "
                        + txRes.getResourceItems().get(0).getStateMapping().get(ResourceItem.State.confirmSuccess)
                        + ", unit test, "
                        + today + "]",
                dbValue);
    }

    @Test
    public void testCancelCommitFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().noTry().throwUnrecoverableExWhenCommit());
        txRes.getResourceItems().add(MockResourceItem.buildMock().noTry().noCancel());
        txRes.init();

        CommonTransactionContext context = buildContext("997");

        try {
            txRunner.run(context);
            fail("No SOATxUnrecoverableException be thrown!");
        } catch (SOATxUnrecoverableException e) {
            log.info("got SOATxUnrecoverableException successful!");
        }

        verify(txRes.getResourceItems().get(0), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("997");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //the state is the canceled successful of the first item, means all items have been canceled.
        Assert.assertEquals("[997, "
                        + txRes.getResourceItems().get(0).getStateMapping().get(ResourceItem.State.cancelSuccess)
                        + ", unrecoverable exception, "
                        + today + "]",
                dbValue);
    }

    @Test
    public void testInterruptTryFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().throwUnawareExWhenTry());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry());
        txRes.init();

        CommonTransactionContext context = buildContext("996");

        try {
            txRunner.run(context);
            fail("No SOATxUnawareException be thrown!");
        } catch (SOATxUnawareException e) {
            log.info("got SOATxUnawareException successful!");
        }

        verify(txRes.getResourceItems().get(0), times(0)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("996");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Assert.assertEquals("[996, "
                        + txRes.getResourceItems().get(0).getStateMapping().get(ResourceItem.State.trySuccess)
                        + ", unit test, "
                        + today + "]",
                dbValue);
    }

    @Test
    public void testCancelTryFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().throwUnrecoverableExWhenTry());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry());
        txRes.init();

        CommonTransactionContext context = buildContext("995");

        try {
            txRunner.run(context);
            fail("No SOATxUnrecoverableException be thrown!");
        } catch (SOATxUnrecoverableException e) {
            log.info("got SOATxUnrecoverableException successful!");
        }

        verify(txRes.getResourceItems().get(0), times(0)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(0)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("995");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Assert.assertEquals("[995, "
                        + txRes.getResourceItems().get(0).getStateMapping().get(ResourceItem.State.cancelSuccess)
                        + ", unrecoverable exception, "
                        + today + "]",
                dbValue);
    }

    @Test
    public void testInterruptCancelFlow() throws SQLException {
        log.info("begin test tx runner");
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry().throwUnawareExWhenCancel());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry().throwUnrecoverableExWhenCommit());
        txRes.init();

        CommonTransactionContext context = buildContext("994");

        try {
            txRunner.run(context);
            fail("No SOATxUnawareException be thrown!");
        } catch (SOATxUnawareException e) {
            log.info("got SOATxUnawareException successful!");
        }

        verify(txRes.getResourceItems().get(0), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).confirmTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(1)).confirmTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(0)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).cancelTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(1)).cancelTx(any(TransactionContext.class));

        verify(txRes.getResourceItems().get(0), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(1), times(1)).tryTx(any(TransactionContext.class));
        verify(txRes.getResourceItems().get(2), times(1)).tryTx(any(TransactionContext.class));

        String dbValue = TestTableManager.query("994");
        log.info(dbValue);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Assert.assertEquals("[994, "
                        + txRes.getResourceItems().get(1).getStateMapping().get(ResourceItem.State.cancelFailed)
                        + ", unaware exception, "
                        + today + "]",
                dbValue);
    }

    private CommonTransactionContext buildContext(String id) {
        CommonTransactionContext context = new CommonTransactionContext();
        context.setKey("TEST_TX");
        context.setDataSource(HsqlDatabase.getInstance().getDataSource());
        context.setId(id);
        context.setState(txRes.getBeginningState());
        return context;
    }

    private TransactionResource buildTxRes() {
        TransactionResource txRes = new TransactionResource();
        txRes.setStateGenerator(new SeqStateGenerator());
        txRes.setTable("test_tcctx");
        txRes.setStateCol("status");
        txRes.setIdCol("id");
        txRes.setHandleTimeCol("last_handle_time");
        txRes.setMsgCol("process_msg");
        txRes.setMsgMaxLength(500);
        List<ResourceItem> resourceItems = new ArrayList<>();
        txRes.setResourceItems(resourceItems);
        return txRes;
    }

}
