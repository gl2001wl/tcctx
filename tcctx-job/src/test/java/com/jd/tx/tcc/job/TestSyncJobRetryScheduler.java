package com.jd.tx.tcc.job;

import org.junit.Before;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/27
 */
public class TestSyncJobRetryScheduler {

    private SyncJobRetryScheduler syncJobRetryScheduler;

    @Before
    public void setup() {
        syncJobRetryScheduler = new SyncJobRetryScheduler();
    }

}
