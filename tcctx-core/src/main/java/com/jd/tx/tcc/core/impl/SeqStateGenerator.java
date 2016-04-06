package com.jd.tx.tcc.core.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.jd.tx.tcc.core.ResourceItem;
import com.jd.tx.tcc.core.StateGenerator;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
public class SeqStateGenerator implements StateGenerator {

    @Override
    public void generatorStates(List<ResourceItem> resourceItems) {
        Validate.notNull(resourceItems);

        for (int i = 1; i < resourceItems.size(); i++) {
            ResourceItem resourceItem = resourceItems.get(i);
            BiMap<ResourceItem.State, String> stateMap = new ImmutableBiMap.Builder<ResourceItem.State, String>()
                    .put(ResourceItem.State.begin, (i * 10) + "0")
                    .put(ResourceItem.State.trySuccess, (i * 10 + 1) + "1")
                    .put(ResourceItem.State.tryFailed, (i * 10 + 1) + "0")
                    .put(ResourceItem.State.confirmSuccess, (i * 10 + 2) + "1")
                    .put(ResourceItem.State.confirmFailed, (i * 10 + 2) + "0")
                    .put(ResourceItem.State.cancelSuccess, (i * 10 + 3) + "1")
                    .put(ResourceItem.State.cancelFailed, (i * 10 + 3) + "0")
                    .build();
            resourceItem.setStateMapping(stateMap);
        }
    }

}
