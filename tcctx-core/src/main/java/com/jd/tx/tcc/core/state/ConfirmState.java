package com.jd.tx.tcc.core.state;

import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.ResourceItemLinkedList;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class ConfirmState implements TransactionState {

    public static final ConfirmState instance = new ConfirmState();

    @Override
    public void handle(StateContext stateContext) {
        try {
            ResourceItemLinkedList linkedItem = stateContext.getItemLinkedList();

            if (linkedItem.getItem().getStateMapping() == null ||
                    !linkedItem.getItem().getStateMapping().containsKey(ResourceItem.State.confirmSuccess) ||
                    !linkedItem.getItem().getStateMapping().get(ResourceItem.State.confirmSuccess)
                            .equals(
                                    stateContext.getTransactionContext().getState()
                            )) {
                TransactionAction.confirmAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
            stateContext.getTransactionContext().setState(null);

            while (linkedItem.hasNext()) {
                linkedItem = linkedItem.getNext();
                stateContext.setItemLinkedList(linkedItem);
                TransactionAction.confirmAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
        } catch (SOATxUnrecoverableException e) {
            //unrecoverable exception happened, cancel all.
            stateContext.moveToTail();
            stateContext.setState(CancelState.instance);
            stateContext.invokeState();
            //tell the client this transaction failed.
            throw e;
        } catch (Throwable e) {
            //unaware exception happened, shut down process and wait for retry.
            throw new SOATxUnawareException(e.getMessage(), e);
        }
    }
}
