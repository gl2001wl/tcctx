package com.jd.tx.tcc.core.impl;


import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/17
 */
interface TransactionAction {

    TryAction tryAction = new TryAction();

    ConfirmAction confirmAction = new ConfirmAction();

    CancelAction cancelAction = new CancelAction();

    void action(TransactionContext context, TransactionResource resource, ResourceItem item) throws Throwable;

}

class TryAction implements TransactionAction {

    @Override
    public void action(TransactionContext context, TransactionResource resource, ResourceItem item) throws Throwable {
        try {
            if (item.hasTry()) {
                item.tryTx(context);
            }
        } catch (SOATxUnrecoverableException e) {
            //when unrecoverable exception happened, updateState the state to try failed for canceling all before it.
            if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.tryFailed)) {
                JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.tryFailed), e.getMessage());
            }
            throw e;
        } catch (Throwable e) {
            throw e;
        }
        if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.trySuccess)) {
            JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.trySuccess), null);
        }
    }

}

class ConfirmAction implements TransactionAction {

    @Override
    public void action(TransactionContext context, TransactionResource resource, ResourceItem item) throws Throwable {
        try {
            item.confirmTx(context);
        } catch (SOATxUnrecoverableException e) {
            //when unrecoverable exception happened, updateState the state to confirm failed for canceling all.
            if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.confirmFailed)) {
                JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.confirmFailed), e.getMessage());
            }
            throw e;
        } catch (Throwable e) {
            throw e;
        }
        if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.confirmSuccess)) {
            JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.confirmSuccess), null);
        }
    }

}

class CancelAction implements TransactionAction {

    @Override
    public void action(TransactionContext context, TransactionResource resource, ResourceItem item) throws Throwable {
        try {
            if (item.hasCancel()) {
                item.cancelTx(context);
            }
        } catch (Throwable e) {
            if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.cancelFailed)) {
                JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.cancelFailed), e.getMessage());
            }
            throw e;
        }
        if (item.getIgnoreUpdateState() == null || !item.getIgnoreUpdateState().contains(ResourceItem.State.cancelSuccess)) {
            JDBCHelper.updateState(context, resource, (String) item.getStateMapping().get(ResourceItem.State.cancelSuccess), null);
        }
    }

}
