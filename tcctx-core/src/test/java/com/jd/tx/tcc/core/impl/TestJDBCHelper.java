package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionManager;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.TransactionRunner;
import com.jd.tx.tcc.core.entity.TransactionEntity;
import com.jd.tx.tcc.core.query.TransactionQuery;
import com.jd.tx.tcc.core.sync.SyncTransactionRunner;
import com.jd.tx.tcc.core.test.hsql.HsqlDatabase;
import com.jd.tx.tcc.core.test.hsql.TestTableManager;
import com.jd.tx.tcc.core.test.mock.MockResourceItem;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/27
 */
public class TestJDBCHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TestJDBCHelper.class);

    private TransactionRunner txRunner;

    private TransactionResource txRes;

    private TransactionManager txManager;

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
    public void testFindTimeout() {
        txRes.getResourceItems().add(MockResourceItem.buildMock());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry().throwUnawareExWhenCancel());
        txRes.getResourceItems().add(MockResourceItem.buildMock().emptyTry().throwUnrecoverableExWhenCommit());
        txRes.init();

        TransactionQuery query = new TransactionQuery(buildContext(), txRes)
                .setMinutesBefore(1)
                .setQueryRows(200);
        query.setExcludeStates(buildExcludeStates(txRes.getResourceItems()));

        List<TransactionEntity> timeoutItems = JDBCHelper.findTimeoutItems(query);
        for (TransactionEntity entity : timeoutItems) {
            LOG.info(entity.toString());
        }
    }

    private CommonTransactionContext buildContext() {
        CommonTransactionContext context = new CommonTransactionContext();
        context.setKey("TEST_TX");
        context.setDataSource(HsqlDatabase.getInstance().getDataSource());
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

    private List<String> buildExcludeStates(List<ResourceItem> resourceItems) {
        List<String> excludeStates = new ArrayList<>();
        if (resourceItems.get(resourceItems.size() - 1).getStateMapping().get(ResourceItem.State.confirmSuccess) != null) {
            excludeStates.add((String) resourceItems.get(resourceItems.size() - 1).getStateMapping().get(ResourceItem.State.confirmSuccess));
        }
        if (resourceItems.get(0).getStateMapping().get(ResourceItem.State.cancelSuccess) != null) {
            excludeStates.add((String) resourceItems.get(0).getStateMapping().get(ResourceItem.State.cancelSuccess));
        }
        return excludeStates;
    }

}
