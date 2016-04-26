package com.jd.tx.tcc.core.test.mock;

import com.google.common.collect.BiMap;
import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;
import com.jd.tx.tcc.core.test.hsql.HsqlDatabase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/25
 */
public class MockResourceItem implements ResourceItem {

    private static final Logger LOG = LoggerFactory.getLogger(MockResourceItem.class);

    private BiMap<State, String> stateMapping;

    private MockResourceItem() {}

    @Override
    public boolean hasTry() {
        return true;
    }

    @Override
    public boolean hasCancel() {
        return true;
    }

    @Override
    public void tryTx(TransactionContext context) {
        String insertSql = "insert into test_tcctx (id, status, process_msg, last_handle_time) values ('" +
                context.getId() +"', '" +
                context.getState() + "', " +
                "'unit test', " +
                "now()" +
                " )";
        try {
            LOG.info("insert sql: " + insertSql);
            HsqlDatabase.getInstance().executeSql(insertSql);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new SOATxUnrecoverableException(e);
        }
    }

    @Override
    public void confirmTx(TransactionContext context) {
        LOG.info("invoke confirm");
    }

    @Override
    public void cancelTx(TransactionContext context) {
        LOG.info("invoke cancelTx");
    }

    @Override
    public List<State> getIgnoreUpdateState() {
        return null;
    }

    @Override
    public BiMap<State, String> getStateMapping() {
        return stateMapping;
    }

    @Override
    public Integer getStateIndex() {
        return null;
    }

    @Override
    public void setStateMapping(BiMap stateMapping) {
        this.stateMapping = stateMapping;
    }

    public static MockResourceItem buildMock() {
        MockResourceItem item = new MockResourceItem();
        MockResourceItem spyItem = spy(item);
        return spyItem;
    }

    public MockResourceItem emptyTry() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                LOG.info("called try with arguments: " + args);
                return null;
            }
        }).when(this).tryTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem emptyCommit() {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called normal item with arguments: " + args);
                return null;
            }
        }).when(this).confirmTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem noTry() {
        when(this.hasTry()).thenReturn(false);
        return this;
    }

    public MockResourceItem noCancel() {
        when(this.hasCancel()).thenReturn(false);
        return this;
    }

    public MockResourceItem throwUnawareExWhenCommit() {
        doThrow(new SOATxUnawareException("unaware exception"))
                .when(this).confirmTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem throwUnrecoverableExWhenCommit() {
        doThrow(new SOATxUnrecoverableException("unrecoverable exception"))
                .when(this).confirmTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem throwUnawareExWhenTry() {
        doThrow(new SOATxUnawareException("unaware exception"))
                .when(this).tryTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem throwUnrecoverableExWhenTry() {
        doThrow(new SOATxUnrecoverableException("unrecoverable exception"))
                .when(this).tryTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem throwUnawareExWhenCancel() {
        doThrow(new SOATxUnawareException("unaware exception"))
                .when(this).cancelTx(any(TransactionContext.class));
        return this;
    }

    public MockResourceItem throwUnrecoverableExWhenCancel() {
        doThrow(new SOATxUnrecoverableException("unrecoverable exception"))
                .when(this).cancelTx(any(TransactionContext.class));
        return this;
    }

}
