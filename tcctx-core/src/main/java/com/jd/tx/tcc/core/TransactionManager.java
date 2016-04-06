package com.jd.tx.tcc.core;

import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

import java.util.Map;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public class TransactionManager {

    @Setter
    private Map<String, TransactionResource> resourcesMap;

    public TransactionResource getResource(@NonNull String key) {
        Validate.notEmpty(resourcesMap);
        return resourcesMap.get(key);
    }

}
