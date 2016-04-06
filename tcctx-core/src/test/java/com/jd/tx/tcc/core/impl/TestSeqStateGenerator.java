package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.ResourceItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
public class TestSeqStateGenerator {

    private SeqStateGenerator generator = new SeqStateGenerator();

    @Mock
    private ResourceItem resourceItem;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStateGenerator() {

    }

}
