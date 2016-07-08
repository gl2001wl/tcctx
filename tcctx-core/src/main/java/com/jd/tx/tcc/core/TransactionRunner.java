package com.jd.tx.tcc.core;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public interface TransactionRunner {

    /**
     * Begin execute a TCC transaction
     *
     * @param context
     */
    void run(TransactionContext context);

    /**
     * Set the transactionManager before execute
     *
     * @param transactionManager
     */
    void setTransactionManager(TransactionManager transactionManager);

    /**
     * Return transactionManager
     *
     * @return
     */
    TransactionManager getTransactionManager();

}
