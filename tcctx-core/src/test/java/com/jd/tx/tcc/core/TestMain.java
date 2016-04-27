package com.jd.tx.tcc.core;

import com.jd.tx.tcc.core.impl.TestSeqStateGenerator;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/27
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestSeqStateGenerator.class,
        TestResourceItemLinkedList.class,
        TestTransactionRunner.class
})
public class TestMain {

}
