package com.jd.tx.tcc.core.retry;

import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionManager;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.TransactionRunner;
import com.jd.tx.tcc.core.entity.TransactionEntity;
import com.jd.tx.tcc.core.impl.CommonTransactionContext;
import com.jd.tx.tcc.core.query.TransactionQuery;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 *  An execution scheduler for retry timeout tx job.
 * @author Leon Guo
 *         Creation Date: 2016/5/17
 */
@Slf4j
@RequiredArgsConstructor
public class RetryJob {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private TransactionRunner transactionRunner;

    private String key;

    private DataSource dataSource;

    @Setter
    private long initialDelay = 0;

    @Setter
    private long period = 2;

    @Setter
    private TimeUnit scheduleTimeUnit = TimeUnit.MINUTES;

    @Setter
    private int fetchRows = 200;

    @Setter
    private int timeoutMinutes = 2;

    private String lastId = null;

    public void start() {
        Validate.notEmpty(key);
        Validate.notNull(dataSource);
        Validate.notNull(transactionRunner);

        scheduler.scheduleAtFixedRate(new RetryRunner(), initialDelay, period, scheduleTimeUnit);
    }

    class RetryRunner implements Runnable {

        @Override
        public void run() {
            List<TransactionEntity> timeoutTx = fetchData();
            while (CollectionUtils.isNotEmpty(timeoutTx)) {
                executeTx(timeoutTx);
                timeoutTx = fetchData();
            }
        }

        private List<TransactionEntity> fetchData() {
            TransactionManager transactionManager = transactionRunner.getTransactionManager();
            TransactionResource resource = transactionManager.getResource(key);

            CommonTransactionContext context = new CommonTransactionContext();
            context.setKey(key);
            context.setDataSource(dataSource);

            TransactionQuery query = new TransactionQuery(context, resource)
                    .setMinutesBefore(timeoutMinutes)
                    .setQueryRows(fetchRows);
            if (StringUtils.isNotBlank(lastId)) {
                query.setLastId(lastId);
            }
            query.setExcludeStates(buildExcludeStates(resource.getResourceItems()));
            List<TransactionEntity> timeoutItems = query.query();

            if (CollectionUtils.isNotEmpty(timeoutItems)) {
                lastId = timeoutItems.get(timeoutItems.size() - 1).getId();
            } else {
                //If no more data, set the lastId to null, make it load data from the very beginning in the next schedule time.
                lastId = null;
            }
            return timeoutItems;
        }

        private void executeTx(List<TransactionEntity> data) {
            if (CollectionUtils.isEmpty(data)) {
                return;
            }

            for (TransactionEntity entity : data) {
                CommonTransactionContext txContext = new CommonTransactionContext();

                txContext.setKey(key);
                txContext.setDataSource(dataSource);
                txContext.setId(entity.getId());
                txContext.setState(entity.getState());

                try {
                    transactionRunner.run(txContext);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        private List<String> buildExcludeStates(List<ResourceItem> resourceItems) {
            List<String> excludeStates = new ArrayList<>();
            if (resourceItems.get(resourceItems.size() - 1).getStateMapping().get(ResourceItem.State.confirmSuccess) != null) {
                excludeStates.add((String) resourceItems.get(resourceItems.size() - 1).getStateMapping().get(ResourceItem.State.confirmSuccess));
            }
            if (resourceItems.get(0).getStateMapping().get(ResourceItem.State.cancelSuccess) != null) {
                excludeStates.add((String) resourceItems.get(0).getStateMapping().get(ResourceItem.State.cancelSuccess));
            }
            return excludeStates;
        }
    }

}
