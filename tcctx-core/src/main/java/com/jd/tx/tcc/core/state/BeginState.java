package com.jd.tx.tcc.core.state;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/26
 */
public class BeginState implements TransactionState {

    public static final BeginState instance = new BeginState();

    @Override
    public void handle(StateContext stateContext) {
        stateContext.moveToHead();
        stateContext.setState(TryState.instance);
        stateContext.invokeState();
    }

}
