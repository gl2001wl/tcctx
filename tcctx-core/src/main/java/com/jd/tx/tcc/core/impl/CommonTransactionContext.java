package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.TransactionContext;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.sql.DataSource;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/17
 */
@EqualsAndHashCode
@ToString
public class CommonTransactionContext<T> implements TransactionContext<T> {

    @Setter private DataSource dataSource;

    @Setter private String key;

    @Setter private String id;

    @Setter private String state;

    @Setter private T resourceObject;

    @Setter private int timeoutForRetry;

    @Setter private int selectLimitForRetry;

    public CommonTransactionContext() {
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public T getResourceObject() {
        return resourceObject;
    }

//    @Override
//    public int getTimeoutForRetry() {
//        return timeoutForRetry;
//    }
//
//    @Override
//    public int getSelectLimitForRetry() {
//        return selectLimitForRetry;
//    }

}
