package com.minepalm.syncer.core;

import com.minepalm.syncer.api.*;
import com.minepalm.syncer.core.mysql.MySQLSyncedController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedSynced<T> implements Synced<T> {

    private static final AtomicInteger totalCount = new AtomicInteger(0);

    private static final long INFINITE = -1;

    private final T handle;
    private final SyncToken<T> token;
    private final Parker parker;
    private final MySQLSyncedController controller;
    private final HoldServerRegistry holderRegistry;
    private final SyncPubSub pubSub;

    private final AtomicBoolean acquired = new AtomicBoolean(false);
    private final Unsafe unsafe;

    protected DistributedSynced(T handle,
                             SyncToken<T> token,
                             Parker parker,
                             MySQLSyncedController controller,
                             HoldServerRegistry holderRegistry,
                             SyncPubSub pubSub){
        this.handle = handle;
        this.token = token;
        this.parker = parker;
        this.controller = controller;
        this.holderRegistry = holderRegistry;
        this.pubSub = pubSub;
        this.unsafe = new UnsafeImpl(this, pubSub, controller);
    }

    @RequiredArgsConstructor
    private class UnsafeImpl implements Synced.Unsafe{

        private final Synced<T> handle;
        private final SyncPubSub pubSub;
        private final MySQLSyncedController controller;

        @Override
        public void hold() {
            controller.holdUnsafe(System.currentTimeMillis()+5000L);
            pubSub.openSubscription(handle);
        }

        @Override
        public void release() {
            controller.releaseUnsafe();
            pubSub.closeSubscription(handle);
        }

        @Override
        public void set(long time) {
            controller.setTimeout(time);
        }


    }

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
    public CompletableFuture<HoldServer> getHoldServer() {
        return controller.getHoldServer().thenApply(holderRegistry::getHolder);
    }

    @Override
    public CompletableFuture<Boolean> isHold() {
        return controller.isHold();
    }

    @Override
    public CompletableFuture<Boolean> updateTimeout(long timeToAdd) {
        return controller.updateTimeout(timeToAdd);
    }

    @Override
    public CompletableFuture<Boolean> setTimeout(long time) {
        return controller.setTimeout(time);
    }

    @Override
    public void hold(Duration duration) throws ExecutionException, InterruptedException {
        try {
            hold(duration, INFINITE);
        }catch (TimeoutException ignored){

        }
    }

    @Override
    public void hold(Duration duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        tryAcquireLock(currentTime(), duration.toMillis(), timeout);
    }

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    private void tryAcquireLock(long issuedTime, long duration, long timeout) throws InterruptedException, TimeoutException {
        long timeoutTime = issuedTime + timeout;

        totalCount.addAndGet(1);

        if(timeout < 0){
            timeoutTime = Long.MAX_VALUE;
        }

        long beginTime = System.currentTimeMillis();

        while(!controller.tryHold(duration)){
            if(timeoutTime <= beginTime){
                totalCount.addAndGet(-1);
                throw new TimeoutException();
            }

            String holderName = controller.getHoldServer().join();

            if(holderName != null){
                boolean subscribedCompleted = sendRequestSubscription(holderName).join();

                if(subscribedCompleted) {
                    try {
                        park(timeoutTime - beginTime);
                    }catch (TimeoutException exception){
                        totalCount.addAndGet(-1);
                        throw exception;
                    }
                }
            }
            //TODO:
            // 현재 하나의 쓰레드에서 락을 얻고 기다리는 행동을 계속 하는데,
            // 이 부분을 Publish-subscribe 관계로 개선할수 있어 보임.ㅅ
            Thread.currentThread().wait(50L);
        }

        totalCount.addAndGet(-1);
        this.acquired.set(true);
        pubSub.openSubscription(this);
    }

    private CompletableFuture<Boolean> sendRequestSubscription(String holderName){
        return holderRegistry.getHolder(holderName).sendSubscribeWaiting(this);
    }

    @Override
    public void release() {
        if(controller.release(currentTime())) {
            parker.releaseExceptionally();
            pubSub.invokeRetryLock(this);
            pubSub.closeSubscription(this);
            this.acquired.set(false);
        }
    }

    @Override
    public Unsafe unsafe(){
        return unsafe;
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

    private long currentTime() {
        return System.currentTimeMillis();
    }
}
