package com.jd.tx.tcc.core.exception;

/**
 * Unaware exception happened,
 * can't know current resource item acton successful or not,
 * need retry until get real state.
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public class SOATxUnawareException extends RuntimeException {

    public SOATxUnawareException() {
    }

    public SOATxUnawareException(Throwable e) {
        super(e);
    }

    public SOATxUnawareException(String msg) {
        super(msg);
    }

    public SOATxUnawareException(String msg, Throwable e) {
        super(msg, e);
    }

}
