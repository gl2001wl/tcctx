package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/17
 */
public class JDBCHelper {

    /**
     * Default timeout is 2 minutes.
     */
    private static final int DEFAULT_TIMEOUT = 2;

    /**
     * Default max retry rows for once is 100.
     */
    private static final int DEFAULT_RETRY_ROWS = 100;

    /**
     * Update the state of current transaction.<br>
     * <p>Shouldn't throw SOATxUnrecoverableException.
     * If updateState state failed, just wait for retry.
     * </p>
     * @param context
     * @param resource
     * @param state
     */
    public static void updateState(@NonNull TransactionContext context,
                            @NonNull TransactionResource resource,
                            @NonNull String state,
                            String msg) {
        Validate.notNull(context.getDataSource());
        Validate.notNull(context.getId());
        Validate.notNull(resource.getTable());
        Validate.notNull(resource.getIdCol());
        Validate.notNull(resource.getStateCol());

        List<Object> paramList = new ArrayList<Object>(4);
        StringBuilder sql = new StringBuilder()
                .append("update ")
                .append(resource.getTable())
                .append(" set ")
                .append(resource.getStateCol())
                .append(" = ?");
        paramList.add(state);
        if (StringUtils.isNotBlank(resource.getMsgCol())
                && StringUtils.isNotEmpty(msg)) {
            //updateState msg column.
            sql.append(", ")
                    .append(resource.getMsgCol())
                    .append(" = ?");
            if (resource.getMsgMaxLength() > 0) {
                msg = msg.length() > resource.getMsgMaxLength() ? msg.substring(0, resource.getMsgMaxLength()) : msg;
            }
            paramList.add(msg);
        }
        if (StringUtils.isNotBlank(resource.getHandleTimeCol())) {
            //updateState handle time column to current time.
            sql.append(", ")
                    .append(resource.getHandleTimeCol())
                    .append(" = ?");
            paramList.add(new Date());
        }
        sql.append(" where ").append(resource.getIdCol()).append(" = ?");
        paramList.add(context.getId());

        //use spring jdbc template to updateState the state
        new JdbcTemplate(context.getDataSource()).update(sql.toString(), paramList.toArray());
    }

    /**
     * Return the id and state of timeout transactions.
     * @param context
     * @param resource
     * @param shardingCount
     * @param shardingItems
     * @return
     */
    public static List<Map<String, String>> findTimeoutItems(@NonNull TransactionContext context,
                                                             @NonNull TransactionResource resource,
                                                             int shardingCount,
                                                             List<Integer> shardingItems) {
        Validate.notNull(context.getDataSource());
        Validate.notNull(resource.getTable());
        Validate.notNull(resource.getIdCol());
        Validate.notNull(resource.getStateCol());
        Validate.notNull(resource.getHandleTimeCol());

        StringBuilder sql = new StringBuilder()
                .append("select ")
                .append(resource.getIdCol())
                .append(", ")
                .append(resource.getStateCol())
                .append(" from ")
                .append(resource.getTable())
                .append(" where ")
                .append(resource.getHandleTimeCol())
                .append(" < date_sub(now(), interval ? minute)");

        if (shardingCount > 1) {
            // Need sharding, got mode from sharding count.
            sql.append(" and MOD(UNIX_TIMESTAMP(").append(resource.getHandleTimeCol())
                    .append("), ?) in (");
            char split = 0;
            for (int shardingItem : shardingItems) {
                sql.append(split == 0 ? "" : split).append("?");
                split = ',';
            }
            sql.append(")");
        }
        sql.append(" limit ?");

        List<Object> paramList = new ArrayList<>();
        paramList.add(context.getTimeoutForRetry() < 1 ? DEFAULT_TIMEOUT : context.getTimeoutForRetry());
        if (shardingCount > 1) {
            paramList.add(shardingCount);
            for (Integer shardingItem : shardingItems) {
                paramList.add(shardingItem);
            }
        }
        paramList.add(context.getSelectLimitForRetry() < 1 ? DEFAULT_RETRY_ROWS : context.getSelectLimitForRetry());

        List<Map<String, Object>> list =
                new JdbcTemplate(context.getDataSource()).queryForList(sql.toString(), paramList.toArray());

        List<Map<String, String>> result = new ArrayList<>(list == null ? 0 : list.size());
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        for (Map<String, Object> rowMap : list) {
            Map<String, String> resultMap = new HashMap<>(1);
            resultMap.put(String.valueOf(rowMap.get(resource.getIdCol())),
                    String.valueOf(rowMap.get(resource.getStateCol())));
            result.add(resultMap);
        }
        return result;
    }

}
