package com.jd.tx.tcc.core.impl;

import com.jd.tx.tcc.core.*;
import com.jd.tx.tcc.core.exception.SOATxUnawareException;
import com.jd.tx.tcc.core.exception.SOATxUnrecoverableException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author Leon Guo
 *         Creation Date: 2016/2/16
 */
public class TransactionRunnerImpl implements TransactionRunner {

    @Setter
    @Getter
    private TransactionManager transactionManager;

    @Override
    public void run(@NonNull TransactionContext context) {
        Assert.notNull(context.getId());
        Assert.notNull(context.getKey());
        Assert.notNull(context.getState());
        Assert.notNull(transactionManager);

        TransactionResource resource = transactionManager.getResource(context.getKey());
        for (ResourceItem item : resource.getResourceItems()) {
            if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.begin))) {
                //new one, try from the first one.
                tryNext(context, resource, null);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.trySuccess))) {
                //Current item has tried successful, then try from the next and confirm all.
                tryNext(context, resource, item);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.tryFailed))) {
                //Current item has tried failed, then cancel all items before it.
                cancelBefore(context, resource, item);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.confirmSuccess))) {
                //Current item has confirm successful, then confirm all items after it.
                confirmNext(context, resource, item);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.confirmFailed))) {
                //Current item has confirm failed, then cancel all items.
                cancelAll(context, resource);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.cancelFailed))) {
                //Current item has cancel failed, then cancel itself and all items before it.
                cancel(context, resource, item, true);
            } else if (context.getState().equals(item.getStateMapping().get(ResourceItem.State.cancelSuccess))) {
                //Current item has cancel successful, then cancel all items before it.
                cancelBefore(context, resource, item);
            }
        }

    }

    private void confirmNext(TransactionContext context, TransactionResource resource, ResourceItem item) {
        boolean start = false;
        for (ResourceItem resourceItem : resource.getResourceItems()) {
            if (start || item == null) {
                try {
                    TransactionAction.confirmAction.action(context, resource, resourceItem);
                } catch (SOATxUnrecoverableException e) {
                    //unrecoverable exception happened, cancel all items.
                    cancelAll(context, resource);
                    //tell the client this transaction failed.
                    throw e;
                } catch (Throwable e) {
                    //unaware exception happened, shut down process and wait for retry.
                    throw new SOATxUnawareException(e.getMessage(), e);
                }
            }
            if (item != null && resourceItem.equals(item)) {
                start = true;
            }
        }
    }

    private void cancelBefore(TransactionContext context, TransactionResource resource, ResourceItem item) {
        cancel(context, resource, item, false);
    }

    private void cancel(TransactionContext context, TransactionResource resource, ResourceItem item, boolean include) {
        List<ResourceItem> items = resource.getResourceItems();
        boolean start = false;
        for (int i = items.size() - 1; i > -1; i--) {
            ResourceItem resourceItem = items.get(i);
            if ((start || item == null) && resourceItem.hasCancel()) { // if input item is null, then cancel all.
                try {
                    TransactionAction.cancelAction.action(context, resource, resourceItem);
                } catch (Throwable e) {
                    // When exception happened in cancel action, throw unaware exception and wait for retry,
                    // until cancel successful.
                    throw new SOATxUnawareException(e.getMessage(), e);
                }
            }
            if (item != null && resourceItem.equals(item)) {
                start = true;
                if (include && resourceItem.hasCancel()) {
                    //need cancel itself.
                    try {
                        TransactionAction.cancelAction.action(context, resource, resourceItem);
                    } catch (Throwable e) {
                        //when exception happened in cancel action, throw unaware exception and wait for retry.
                        throw new SOATxUnawareException(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void tryNext(TransactionContext context, TransactionResource resource, ResourceItem item) {
        boolean start = false;
        for (ResourceItem resourceItem : resource.getResourceItems()) {
            if ((start || item == null) && resourceItem.hasTry()) {
                try {
                    TransactionAction.tryAction.action(context, resource, resourceItem);
                } catch (SOATxUnrecoverableException e) {
                    //unrecoverable exception happened, cancel all items before this one.
                    cancelBefore(context, resource, resourceItem);
                    //tell the client this transaction failed.
                    throw e;
                } catch (Throwable e) {
                    //unaware exception happened, shut down process and wait for retry.
                    throw new SOATxUnawareException(e.getMessage(), e);
                }
            }
            if (item != null && resourceItem.equals(item)) {
                start = true;
            }
        }
        //confirm all items after try successfully.
        confirmNext(context, resource, null);
    }

    private void cancelAll(TransactionContext context, TransactionResource resource) {
        cancelBefore(context, resource, null);
    }

}
