package com.jd.tx.tcc.core;

import lombok.*;

import java.util.Iterator;
import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
@AllArgsConstructor
@Getter
@Setter
public class ResourceItemLinkedList implements Iterable<ResourceItem> {

    private ResourceItemLinkedList pre;

    private ResourceItem item;

    private ResourceItemLinkedList next;

    public boolean hasPre() {
        return pre != null;
    }

    public boolean hasNext() {
        return next != null;
    }

    public static ResourceItemLinkedList build(List<ResourceItem> resourceItems) {
        Object[] objects = resourceItems.toArray();
        ResourceItemLinkedList first = null;
        ResourceItemLinkedList linkedList = null;
        for (int i = 0; i < objects.length; i++) {
            ResourceItem resourceItem = (ResourceItem) objects[i];
            if (i == 0) {
                linkedList = new ResourceItemLinkedList(null, resourceItem, null);
                first = linkedList;
                continue;
            }
            linkedList.next = new ResourceItemLinkedList(linkedList, resourceItem, null);
            linkedList = linkedList.next;
        }
        return first;
    }

    public ResourceItemLinkedList getHead() {
        ResourceItemLinkedList linkedList = this;
        while (linkedList.hasPre()) {
            linkedList = linkedList.getPre();
        }
        return linkedList;
    }

    public ResourceItemLinkedList getTail() {
        ResourceItemLinkedList linkedList = this;
        while (linkedList.hasNext()) {
            linkedList = linkedList.getNext();
        }
        return linkedList;
    }

    @Override
    public Iterator<ResourceItem> iterator() {
        return null;
    }
}
