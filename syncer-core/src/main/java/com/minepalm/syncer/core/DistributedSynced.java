package com.minepalm.syncer.core;

import com.minepalm.syncer.api.*;
import com.minepalm.syncer.core.mysql.MySQLSyncedController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DistributedSynced<T> implements Synced<T> {

    private static final long INFINITE = -1;

    private final T handle;
    private final SyncToken<T> token;
    private final Parker parker;
    private final MySQLSyncedController controller;
    private final SyncHolderRegistry holderRegistry;
    private final SyncPubSub pubSub;

    private final AtomicBoolean acquired = new AtomicBoolean(false);

    @Override
    public T get() {
        if(acquired.get()) {
            return handle;
        }else{
            throw new IllegalStateException("the object access denied not acquired lock");
        }
    }

    @Override
    public String getObjectKey() {
        return token.getObjectId(handle);
    }

    @Override
    public CompletableFuture<SyncHolder> getHoldServer() {
        return controller.getHoldServer().thenApply(holderRegistry::getHolder);
    }

    @Override
    public CompletableFuture<Boolean> isHold() {
        return controller.isHold(this.holderRegistry.getLocalHolder().getName());
    }

    @Override
    public CompletableFuture<Boolean> updateTimeout(long timeToAdd) {
        return controller.updateTimeout(generateData(getObjectKey()).setTime(timeToAdd));
    }

    //@Override
    /*
    public synchronized CompletableFuture<Boolean> transferHold(SyncHolder holder) {
        return controller.holdUnsafeAsync(new HoldData(getObjectKey(), holder.getName(), System.currentTimeMillis() + 5000L))
                .thenCompose(ignored -> controller.getHoldServer())
                .thenCompose(serverName -> {
                    if(serverName == null){
                        return CompletableFuture.completedFuture(false);
                    } else{
                        return holderRegistry.getHolder(serverName).sendTransferHolding(this);
                    }

                })
                .thenApply(result -> {
                    pubSub.invokeRetryLock(this);
                    return result;
                });
    }
     */

    @Override
    public synchronized void hold(Duration duration) throws ExecutionException, InterruptedException {
        try {
            hold(duration, INFINITE);
        }catch (TimeoutException ignored){

        }
    }

    @Override
    public synchronized void hold(Duration duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        tryAcquireLock(System.currentTimeMillis(), duration.toMillis(), timeout);
    }

    private synchronized boolean tryAcquireLock(long issuedTime, long duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        long timeoutTime = issuedTime + timeout;
        long timeoutAmount = timeout;

        if(timeout < 0){
            timeoutTime = Long.MAX_VALUE;
            timeoutAmount = Long.MAX_VALUE - issuedTime;
        }

        long currentTime = System.currentTimeMillis();

        if(timeoutTime <= currentTime){
            throw new TimeoutException();
        }

        boolean acquiredLock = controller.tryHold(generateData(this.getObjectKey()).setTime(currentTime), duration);

        if(!acquiredLock){
            String holderName = controller.getHoldServer().get();

            if(holderName == null){
                return tryAcquireLock(issuedTime, duration, timeoutAmount);
            }

            boolean subscribedCompleted = sendRequestSubscription(holderName).get();

            if(subscribedCompleted) {
                park(timeoutTime - currentTime);
            }

            return tryAcquireLock(issuedTime, duration, timeoutAmount);
        }else{
            this.acquired.set(true);
            pubSub.openSubscription(this);
            return true;
        }
    }

    private CompletableFuture<Boolean> sendRequestSubscription(String holderName){
        return holderRegistry.getHolder(holderName).sendSubscribeWaiting(this);
    }

    @Override
    public void release() throws ExecutionException, InterruptedException {
        if(acquired.get()) {
            controller.release(generateData(this.getObjectKey()).setCurrentTime());
            parker.releaseExceptionally();
            pubSub.invokeRetryLock(this);
            pubSub.closeSubscription(this);
            this.acquired.set(false);
        }
    }

    protected void releasePark(){
        parker.release();
    }

    protected void park(long mills) throws TimeoutException {
        if(mills <= 0){
            throw new TimeoutException();
        }else{
            parker.park(mills);
        }
    }

    private HoldData generateData(String objectId){
        return new HoldData(objectId, holderRegistry.getLocalHolder().getName(), 0L);
    }
}
