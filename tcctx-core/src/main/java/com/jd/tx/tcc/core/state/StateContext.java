package com.jd.tx.tcc.core.state;

import com.jd.tx.tcc.core.ResourceItemLinkedList;
import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;
import lombok.*;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
@Getter
@Setter
@RequiredArgsConstructor
public class StateContext {

    @NonNull
    private TransactionContext transactionContext;

    @NonNull
    private TransactionResource resource;

    @NonNull
    private ResourceItemLinkedList itemLinkedList;

    @NonNull
    private TransactionState state;

    public void invokeState() {
        if (state != null) {
            state.handle(this);
        }
    }

    public void moveToHead() {
        if (itemLinkedList != null) {
            itemLinkedList = itemLinkedList.getHead();
        }
    }

    public void moveToTail() {
        if (itemLinkedList != null) {
            itemLinkedList = itemLinkedList.getTail();
        }
    }

}
