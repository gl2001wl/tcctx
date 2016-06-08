package com.jd.tx.tcc.core.state;

import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.ResourceItemLinkedList;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class TryState implements TransactionState {

    public final static TryState instance = new TryState();

    @Override
    public void handle(StateContext stateContext) {
        try {
            ResourceItemLinkedList linkedItem = stateContext.getItemLinkedList();

            if (linkedItem.getItem().getStateMapping() == null ||
                    !linkedItem.getItem().getStateMapping().containsKey(ResourceItem.State.trySuccess) ||
                    !linkedItem.getItem().getStateMapping().get(ResourceItem.State.trySuccess)
                    .equals(
                            stateContext.getTransactionContext().getState()
                    )) {
                TransactionAction.tryAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
            stateContext.getTransactionContext().setState(null);

            while (linkedItem.hasNext()) {
                linkedItem = linkedItem.getNext();
                stateContext.setItemLinkedList(linkedItem);
                TransactionAction.tryAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
            stateContext.moveToHead();
            stateContext.setState(ConfirmState.instance);
            stateContext.invokeState();
        } catch (SOATxUnrecoverableException e) {
            //unrecoverable exception happened, cancel.
            if (stateContext.getItemLinkedList().hasPre()) {
                stateContext.setItemLinkedList(stateContext.getItemLinkedList().getPre());
                stateContext.setState(CancelState.instance);
                stateContext.invokeState();
            }
            //tell the client this transaction failed.
            throw e;
        } catch (Throwable e) {
            //unaware exception happened, shut down process and wait for retry.
            throw new SOATxUnawareException(e.getMessage(), e);
        }
    }
}
