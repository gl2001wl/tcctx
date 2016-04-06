package com.jd.tx.tcc.core.exception;

/**
 * Unrecoverable exception happened,
 * means need cancel all executed items directly
 * and return failed.
 * @author Leon Guo
 *         Creation Date: 2016/2/17
 */
public class SOATxUnrecoverableException extends RuntimeException{

    public SOATxUnrecoverableException() {
    }

    public SOATxUnrecoverableException(Throwable e) {
        super(e);
    }

    public SOATxUnrecoverableException(String msg) {
        super(msg);
    }

    public SOATxUnrecoverableException(String msg, Throwable e) {
        super(msg, e);
    }

}
