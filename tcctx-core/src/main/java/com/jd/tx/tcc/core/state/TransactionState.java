package com.jd.tx.tcc.core.state;

import com.jd.tx.tcc.core.ResourceItemLinkedList;
import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public interface TransactionState {

    void handle(StateContext stateContext);

}
