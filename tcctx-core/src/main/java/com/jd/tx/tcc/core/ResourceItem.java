package com.jd.tx.tcc.core;

import com.google.common.collect.BiMap;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public interface ResourceItem<T> {

    /**
     * Return true if has try action
     * @return
     */
    boolean hasTry();

    /**
     * Return true if has cancel action
     * @return
     */
    boolean hasCancel();

    /**
     * Try transaction resource
     * @param context
     */
    void tryTx(TransactionContext<T> context);

    /**
     * Confirm transaction resource
     * @param context
     */
    void confirmTx(TransactionContext<T> context);

    /**
     * Cancel transaction resource
     * @param context
     */
    void cancelTx(TransactionContext<T> context);

    /**
     * Return a map which contains the relationship of state and if needing update it after it.
     * @return
     */
    List<State> getIgnoreUpdateState();

    /**
     * Return the mapping of real state and state key in business system.
     * @return
     */
    BiMap<State, String> getStateMapping();

    /**
     *  Set state mapping  values
     * @param stateMapping
     */
    void setStateMapping(BiMap<State, String> stateMapping);

    /**
     *  Return the index for generate state code, use the index in resource manager if return null.
     * @return
     */
    Integer getStateIndex();

    /**
     * Transaction state for getIfUpdateStateAfterAction()
     */
    enum State {
        begin, trySuccess, tryFailed, confirmSuccess, confirmFailed, cancelSuccess, cancelFailed
    }

}
