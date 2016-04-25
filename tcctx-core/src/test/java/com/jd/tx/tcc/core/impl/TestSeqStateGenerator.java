package com.jd.tx.tcc.core.impl;

import com.google.common.collect.BiMap;
import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.test.mock.MockResourceItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
public class TestSeqStateGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestSeqStateGenerator.class);

    private SeqStateGenerator generator = new SeqStateGenerator();

    @Mock
    private ResourceItem resourceItem;

    private List<ResourceItem> resourceItems;

    @Before
    public void setUp() {
    }

    @Test
    public void testStateGenerator() {
        resourceItems = new ArrayList<>();
        MockResourceItem item1 = new MockResourceItem();
        MockResourceItem item2 = new MockResourceItem();
        MockResourceItem item3 = new MockResourceItem();

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
        resourceItems = new ArrayList<>();
        MockResourceItem item1 = mock(MockResourceItem.class);
        when(item1.getStateIndex()).thenReturn(1);
        doCallRealMethod().when(item1).setStateMapping(any(BiMap.class));
        when(item1.getStateMapping()).thenCallRealMethod();

        MockResourceItem item2 = mock(MockResourceItem.class);
        when(item2.getStateIndex()).thenReturn(2);
        doCallRealMethod().when(item2).setStateMapping(any(BiMap.class));
        when(item2.getStateMapping()).thenCallRealMethod();

        MockResourceItem item3 = mock(MockResourceItem.class);
        when(item3.getStateIndex()).thenReturn(3);
        doCallRealMethod().when(item3).setStateMapping(any(BiMap.class));
        when(item3.getStateMapping()).thenCallRealMethod();

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

}
