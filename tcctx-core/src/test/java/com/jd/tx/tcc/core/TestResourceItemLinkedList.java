package com.jd.tx.tcc.core;

import com.jd.tx.tcc.core.test.mock.MockResourceItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class TestResourceItemLinkedList {

    private List<ResourceItem> resourceItems;

    @Before
    public void setup() {
        resourceItems = new ArrayList<>();
        resourceItems.add(MockResourceItem.buildMock());
        resourceItems.add(MockResourceItem.buildMock());
        resourceItems.add(MockResourceItem.buildMock());
        resourceItems.add(MockResourceItem.buildMock());
    }

    @Test
    public void testBuildLinkedList() {
        ResourceItemLinkedList resourceItemLinkedList = ResourceItemLinkedList.build(resourceItems);
        System.out.println(resourceItemLinkedList);
        Assert.assertEquals(resourceItems.get(0), resourceItemLinkedList.getItem());
        Assert.assertNull(resourceItemLinkedList.getPre());
        Assert.assertEquals(resourceItems.get(1), resourceItemLinkedList.getNext().getItem());
        int i = 1;
        while (resourceItemLinkedList.hasNext()) {
            resourceItemLinkedList = resourceItemLinkedList.getNext();
            System.out.println(resourceItemLinkedList);
            Assert.assertEquals(resourceItems.get(i), resourceItemLinkedList.getItem());
            Assert.assertEquals(resourceItems.get(i - 1), resourceItemLinkedList.getPre().getItem());
            if (i == resourceItems.size() - 1) {
                Assert.assertNull(resourceItemLinkedList.getNext());
            } else {
                Assert.assertEquals(resourceItems.get(i + 1), resourceItemLinkedList.getNext().getItem());
            }
            i++;
        }
        Assert.assertEquals(4, i);
    }

}
