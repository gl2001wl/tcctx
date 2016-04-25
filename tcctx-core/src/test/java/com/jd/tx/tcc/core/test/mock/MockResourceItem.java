package com.jd.tx.tcc.core.test.mock;

import com.google.common.collect.BiMap;
import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionContext;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/25
 */
public class MockResourceItem implements ResourceItem {

    private BiMap<State, String> stateMapping;

    @Override
    public boolean hasTry() {
        return false;
    }

    @Override
    public boolean hasCancel() {
        return false;
    }

    @Override
    public void tryTx(TransactionContext context) {

    }

    @Override
    public void confirmTx(TransactionContext context) {

    }

    @Override
    public void cancelTx(TransactionContext context) {

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
}
