package com.jd.tx.tcc.core;

import javax.sql.DataSource;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public interface TransactionContext<T> {

    /**
     * The data source which save transaction state.
     * @return
     */
    DataSource getDataSource();

    /**
     * The key of this transaction group.
     * @return
     */
    String getKey();

    /**
     * The unique id of current transaction process.
     * @return
     */
    String getId();

    /**
     * Current state.
     * @return
     */
    String getState();

    /**
     * Return the resource object of current transaction.
     * @return
     */
    T getResourceObject();

    /**
     * Return the time out of transaction, need be retried in other async process.
     * @return
     */
//    int getTimeoutForRetry();

    /**
     * The max rows could be loaded for once in retry process.
     * @return
     */
//    int getSelectLimitForRetry();

}
