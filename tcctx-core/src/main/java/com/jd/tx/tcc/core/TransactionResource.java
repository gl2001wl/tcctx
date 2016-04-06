package com.jd.tx.tcc.core;

import lombok.Data;

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

}

