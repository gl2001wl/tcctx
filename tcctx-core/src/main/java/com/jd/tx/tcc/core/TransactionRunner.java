package com.jd.tx.tcc.core;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public interface TransactionRunner {

    void run(TransactionContext context);

    void setTransactionManager(TransactionManager transactionManager);

    TransactionManager getTransactionManager();

}
