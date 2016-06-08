package com.jd.tx.tcc.core;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
@Data
public class TransactionResource {

    private List<ResourceItem> resourceItems;

    private String table;

    private String msgCol;

    private int msgMaxLength;

    private String stateCol;

    private String handleTimeCol;

    private String idCol;

    private StateGenerator stateGenerator;

    /**
     * If need to automatically update state by TCC framework
     */
    private boolean updateState = true;

    /**
     *  Return the beginning state of first tx item.
     *  Return null if doesn't have tx items.
     * @return
     */
    public String getBeginningState() {
        if (CollectionUtils.isEmpty(resourceItems) || resourceItems.get(0).getStateMapping() == null) {
            return null;
        }
        return (String) resourceItems.get(0).getStateMapping().get(ResourceItem.State.begin);
    }

    /**
     *  If use this bean spring, should invoke it in init-method, or invoke it in the very beginning.
     */
    public void init() {
        //If has stateGenerator, then generator state for resource items.
        if (stateGenerator != null && CollectionUtils.isNotEmpty(resourceItems)) {
            stateGenerator.generatorStates(resourceItems);
        }
    }

}

