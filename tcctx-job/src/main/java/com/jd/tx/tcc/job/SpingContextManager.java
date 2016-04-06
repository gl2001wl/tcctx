package com.jd.tx.tcc.job;

import org.springframework.context.ApplicationContext;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/29
 */
public class SpingContextManager {

    private static ApplicationContext context;

    public static ApplicationContext get() {
        return context;
    }

    public static void set(ApplicationContext context) {
        SpingContextManager.context = context;
    }
}
