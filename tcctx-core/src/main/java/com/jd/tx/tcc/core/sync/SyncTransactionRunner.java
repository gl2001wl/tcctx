package com.jd.tx.tcc.core.sync;

import com.jd.tx.tcc.core.*;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;
import com.jd.tx.tcc.core.state.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public class SyncTransactionRunner implements TransactionRunner {

    @Setter
    @Getter
    private TransactionManager transactionManager;

    @Override
    public void run(@NonNull TransactionContext context) {
        Validate.notNull(context.getId());
        Validate.notNull(context.getKey());
        Validate.notNull(context.getState());
        Validate.notNull(transactionManager);

        TransactionResource resource = transactionManager.getResource(context.getKey());
        ResourceItemLinkedList itemLinkedList = ResourceItemLinkedList.build(resource.getResourceItems());
        while (itemLinkedList != null) {
            TransactionState txState = getState(context.getState(), itemLinkedList.getItem());
            if (txState != null) {
                StateContext stateContext = new StateContext(context, resource, itemLinkedList, txState);
                stateContext.invokeState();
                break;
            }

            if (itemLinkedList.hasNext()) {
                itemLinkedList = itemLinkedList.getNext();
            } else {
                itemLinkedList = null;
            }
        }
    }

    private TransactionState getState(String contextState, ResourceItem item) {
        if (contextState.equals(item.getStateMapping().get(ResourceItem.State.begin))) {
            return BeginState.instance;
        }
        if (contextState.equals(item.getStateMapping().get(ResourceItem.State.trySuccess))
                || contextState.equals(item.getStateMapping().get(ResourceItem.State.tryFailed))) {
            return TryState.instance;
        }
        if (contextState.equals(item.getStateMapping().get(ResourceItem.State.confirmSuccess))
                || contextState.equals(item.getStateMapping().get(ResourceItem.State.confirmFailed))) {
            return ConfirmState.instance;
        }
        if (contextState.equals(item.getStateMapping().get(ResourceItem.State.cancelSuccess))
                || contextState.equals(item.getStateMapping().get(ResourceItem.State.cancelFailed))) {
            return CancelState.instance;
        }
        return null;
    }

}
