package com.jd.tx.tcc.core.query;

import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.entity.TransactionEntity;
import com.jd.tx.tcc.core.impl.JDBCHelper;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/11
 */
public class TransactionQuery {

    @Getter
    private TransactionContext context;

    @Getter
    private TransactionResource resource;

    @Getter
    private String lastId;

    @Getter
    private int queryRows = 100;

    @Getter
    private int minutesBefore = 2;

    @Getter
    private int shardingCount = -1;

    @Getter
    private List<Integer> shardingItems;

    public TransactionQuery(@NonNull TransactionContext context, @NonNull TransactionResource resource) {
        this.context = context;
        this.resource = resource;
    }

    public TransactionQuery setLastId(String lastId) {
        this.lastId = lastId;
        return this;
    }

    public TransactionQuery setQueryRows(int queryRows) {
        this.queryRows = queryRows;
        return this;
    }

    public TransactionQuery setMinutesBefore(int minutesBefore) {
        this.minutesBefore = minutesBefore;
        return this;
    }

    public TransactionQuery setSharding(int shardingCount, List<Integer> shardingItems) {
        this.shardingCount = shardingCount;
        this.shardingItems = shardingItems;
        return this;
    }

    public List<TransactionEntity> query() {
        return JDBCHelper.findTimeoutItems(this);
    }

}
