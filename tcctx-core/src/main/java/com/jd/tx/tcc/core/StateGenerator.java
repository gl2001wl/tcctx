package com.jd.tx.tcc.core;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/3/10
 */
public interface StateGenerator {

    void generatorStates(List<ResourceItem> resourceItems);

}
