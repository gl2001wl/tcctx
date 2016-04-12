package com.jd.tx.tcc.service;

import com.alibaba.fastjson.JSON;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.entity.TransactionEntity;
import com.jd.tx.tcc.core.impl.CommonTransactionContext;
import com.jd.tx.tcc.core.query.TransactionQuery;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/11
 */
public class TransactionQueryService {

    @Setter
    private Map<String, DataSource> dataSourceMap;

    @Setter
    private TransactionResource txResource;

    @Setter
    private int timeoutMinutes = 2;

    public String queryTransactionEntities(@NonNull String dataSourceKey, String lastId) {
        Validate.notNull(dataSourceMap);
        Validate.notNull(txResource);

        CommonTransactionContext txContext = new CommonTransactionContext();
        txContext.setDataSource(dataSourceMap.get(dataSourceKey));
        List<TransactionEntity> transactionEntities =
                new TransactionQuery(txContext, txResource)
                        .setQueryRows(50)
                        .setLastId(lastId)
                        .setMinutesBefore(timeoutMinutes)
                        .query();

        return JSON.toJSONStringWithDateFormat(transactionEntities, "yyyyMMdd HH:mm:ss");
    }

    public String queryDataSourceKeys() {
        Validate.notNull(dataSourceMap);
        return JSON.toJSONString(dataSourceMap.keySet());
    }

}
