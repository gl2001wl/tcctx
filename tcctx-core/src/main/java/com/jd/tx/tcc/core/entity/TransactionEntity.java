package com.jd.tx.tcc.core.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/11
 */
@Data
public class TransactionEntity implements Serializable {

    private String id;

    private String state;

    private Date handleTime;

}
