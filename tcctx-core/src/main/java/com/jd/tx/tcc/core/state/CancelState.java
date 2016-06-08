package com.jd.tx.tcc.core.state;

import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.ResourceItemLinkedList;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class CancelState implements TransactionState {

    public static final CancelState instance = new CancelState();

    @Override
    public void handle(StateContext stateContext) {
        try {
            ResourceItemLinkedList linkedItem = stateContext.getItemLinkedList();

            if (linkedItem.getItem().getStateMapping() == null ||
                    !linkedItem.getItem().getStateMapping().containsKey(ResourceItem.State.cancelSuccess) ||
                    !linkedItem.getItem().getStateMapping().get(ResourceItem.State.cancelSuccess)
                            .equals(
                                    stateContext.getTransactionContext().getState()
                            )) {
                TransactionAction.cancelAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
            stateContext.getTransactionContext().setState(null);

            while (linkedItem.hasPre()) {
                linkedItem = linkedItem.getPre();
                stateContext.setItemLinkedList(linkedItem);
                TransactionAction.cancelAction.action(stateContext.getTransactionContext(), stateContext.getResource(), linkedItem.getItem());
            }
        } catch (Throwable e) {
            //unaware exception happened, shut down process and wait for retry.
            throw new SOATxUnawareException(e.getMessage(), e);
        }
    }

}
