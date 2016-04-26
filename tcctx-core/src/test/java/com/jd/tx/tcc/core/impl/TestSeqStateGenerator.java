package com.jd.tx.tcc.core.impl;

import com.google.common.collect.BiMap;
import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.TransactionResource;
import com.jd.tx.tcc.core.test.mock.MockResourceItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
public class TestSeqStateGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestSeqStateGenerator.class);

    private SeqStateGenerator generator;

    private List<ResourceItem> resourceItems;

    @Before
    public void setUp() {
        generator = new SeqStateGenerator();
        resourceItems = new ArrayList<>();
    }

    @Test
    public void testStateGenerator() {
        MockResourceItem item1 = MockResourceItem.buildMock();
        MockResourceItem item2 = MockResourceItem.buildMock();
        MockResourceItem item3 = MockResourceItem.buildMock();

        resourceItems.add(item1);
        resourceItems.add(item2);
        resourceItems.add(item3);

        generator.generatorStates(resourceItems);

        LOG.info(item1.getStateMapping().values().toString());
        Assert.assertEquals("[100, 111, 110, 121, 120, 131, 130]", item1.getStateMapping().values().toString());
        LOG.info(item2.getStateMapping().values().toString());
        Assert.assertEquals("[200, 211, 210, 221, 220, 231, 230]", item2.getStateMapping().values().toString());
        LOG.info(item3.getStateMapping().values().toString());
        Assert.assertEquals("[300, 311, 310, 321, 320, 331, 330]", item3.getStateMapping().values().toString());
    }

    @Test
    public void testInputStateGenerator() {
        MockResourceItem item1 = getMockResourceItem(1);
        MockResourceItem item2 = getMockResourceItem(2);
        MockResourceItem item3 = getMockResourceItem(3);

        resourceItems.add(item1);
        resourceItems.add(item2);
        resourceItems.add(item3);

        generator.generatorStates(resourceItems);

        LOG.info(item1.getStateMapping().values().toString());
        Assert.assertEquals("[100, 111, 110, 121, 120, 131, 130]", item1.getStateMapping().values().toString());
        LOG.info(item2.getStateMapping().values().toString());
        Assert.assertEquals("[200, 211, 210, 221, 220, 231, 230]", item2.getStateMapping().values().toString());
        LOG.info(item3.getStateMapping().values().toString());
        Assert.assertEquals("[300, 311, 310, 321, 320, 331, 330]", item3.getStateMapping().values().toString());
    }

    @Test
    public void testStateGeneratorWithTxRes() {
        MockResourceItem item1 = MockResourceItem.buildMock();
        MockResourceItem item2 = MockResourceItem.buildMock();
        MockResourceItem item3 = MockResourceItem.buildMock();

        resourceItems.add(item1);
        resourceItems.add(item2);
        resourceItems.add(item3);

        TransactionResource tr = new TransactionResource();
        tr.setStateGenerator(generator);
        tr.setResourceItems(resourceItems);
        tr.init();
        Assert.assertEquals("100", tr.getBeginningState());
    }

    private MockResourceItem getMockResourceItem(int value) {
        MockResourceItem item1 = mock(MockResourceItem.class);
        when(item1.getStateIndex()).thenReturn(value);
        doCallRealMethod().when(item1).setStateMapping(any(BiMap.class));
        when(item1.getStateMapping()).thenCallRealMethod();
        return item1;
    }

}
