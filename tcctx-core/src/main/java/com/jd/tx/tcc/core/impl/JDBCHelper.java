package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.TransactionContext;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.entity.TransactionEntity;
import com.jd.tx.tcc.core.query.TransactionQuery;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * Return the entities of timeout transactions.
     * @param query
     * @return
     */
    public static List<TransactionEntity> findTimeoutItems(@NonNull  TransactionQuery query) {
        TransactionContext context = query.getContext();
        Validate.notNull(context);
        Validate.notNull(context.getDataSource());

        TransactionResource resource = query.getResource();
        Validate.notNull(resource);
        Validate.notNull(resource.getTable());
        Validate.notNull(resource.getIdCol());
        Validate.notNull(resource.getStateCol());
        Validate.notNull(resource.getHandleTimeCol());

        StringBuilder sql = new StringBuilder()
                .append("select ")
                .append(resource.getIdCol())
                .append(", ")
                .append(resource.getStateCol())
                .append(", ")
                .append(resource.getHandleTimeCol())
                .append(" from ")
                .append(resource.getTable())
                .append(" where ")
                .append(resource.getHandleTimeCol())
                .append(" < date_sub(now(), interval ? minute)")
                .append(" and ")
                .append(resource.getStateCol())
                .append(" IS NOT NULL");

        if (CollectionUtils.isNotEmpty(query.getExcludeStates())) {
            sql.append(" and ")
                    .append(resource.getStateCol())
                    .append(" not in ('")
                    .append(StringUtils.join(query.getExcludeStates(), "','"))
                    .append("')");
        }
        if (CollectionUtils.isNotEmpty(query.getIncludeStates())) {
            sql.append(" and ")
                    .append(resource.getStateCol())
                    .append(" in ('")
                    .append(StringUtils.join(query.getIncludeStates(), "','"))
                    .append("')");
        }
        if (query.getShardingCount() > 1) {
            // Need sharding, got mode from sharding count.
            sql.append(" and MOD(UNIX_TIMESTAMP(").append(resource.getHandleTimeCol())
                    .append("), ?) in (");
            char split = 0;
            for (int shardingItem : query.getShardingItems()) {
                sql.append(split == 0 ? "" : split).append("?");
                split = ',';
            }
            sql.append(")");
        }
        if (StringUtils.isNotBlank(query.getLastId())) {
            sql.append(" and ")
                    .append(resource.getIdCol())
                    .append(" > ?");
        }
        sql.append(" order by ")
                .append(resource.getIdCol())
                .append(" limit ?");

        List<Object> paramList = new ArrayList<>();
        paramList.add(query.getMinutesBefore() < 1 ? DEFAULT_TIMEOUT : query.getMinutesBefore());
        if (query.getShardingCount() > 1) {
            paramList.add(query.getShardingCount());
            for (Integer shardingItem : query.getShardingItems()) {
                paramList.add(shardingItem);
            }
        }
        if (StringUtils.isNotBlank(query.getLastId())) {
            paramList.add(query.getLastId());
        }
        paramList.add(query.getQueryRows() < 1 ? DEFAULT_RETRY_ROWS : query.getQueryRows());

        List<TransactionEntity> list =
                new JdbcTemplate(context.getDataSource()).query(sql.toString(),
                        paramList.toArray(),
                        new RowMapper() {

                            @Override
                            public TransactionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                                TransactionEntity entity = new TransactionEntity();
                                entity.setId(rs.getString(1));
                                entity.setState(rs.getString(2));
                                entity.setHandleTime(rs.getDate(3));
                                return entity;
                            }

                        });

        return list;
    }

}
