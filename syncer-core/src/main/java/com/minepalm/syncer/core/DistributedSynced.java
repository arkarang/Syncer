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

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DistributedSynced<T> implements Synced<T> {


    private static final long INFINITE = -1;

    private final T handle;
    private final SyncToken<T> token;
    private final Parker parker;
    private final MySQLSyncedController controller;
    private final HoldServerRegistry holderRegistry;
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
    public CompletableFuture<HoldServer> getHoldServer() {
        return controller.getHoldServer().thenApply(holderRegistry::getHolder);
    }

    @Override
    public CompletableFuture<Boolean> isHold() {
        return controller.isHold();
    }

    @Override
    public CompletableFuture<Boolean> updateTimeout(long timeToAdd) {
        return controller.updateTimeout(generateData(getObjectKey()).setTime(timeToAdd));
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
        tryAcquireLock(System.currentTimeMillis(), duration.toMillis(), timeout);
    }

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    private void tryAcquireLock(long issuedTime, long duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        long timeoutTime = issuedTime + timeout;

        totalCount.addAndGet(1);

        if(timeout < 0){
            timeoutTime = Long.MAX_VALUE;
        }

        long beginTime = System.currentTimeMillis();

        while(!controller.tryHold(generateData(this.getObjectKey()).setTime(System.currentTimeMillis()), duration)){
            if(timeoutTime <= beginTime){
                //Logger.getGlobal().info("try failed to acquire lock reach timeout(5000ms) "
                //        +this.getObjectKey()
                //        +", time: "+format.format(new Date()));
                totalCount.addAndGet(-1);
                throw new TimeoutException();
            }

            String holderName = controller.getHoldServer().get();

            if(holderName != null){
                boolean subscribedCompleted = sendRequestSubscription(holderName).get();
                //Logger.getGlobal().info("tried subscribe:"+subscribedCompleted+
                //        ", id: "+this.getObjectKey()+""+", current holder: "+holderName+
                //        ", to acquired server name: "+holderRegistry.getLocalHolder().getName()+
                //        ", time: "+format.format(new Date()));

                if(subscribedCompleted) {
                    try {
                        //Logger.getGlobal().info("park wait to receive release id: "+this.getObjectKey()+", current holder: "+holderName+", " +
                        //        "to acquired server name: "+holderRegistry.getLocalHolder().getName()+", time: "+format.format(new Date()));
                        park(timeoutTime - beginTime);
                    }catch (TimeoutException exception){
                        //Logger.getGlobal().info("park timeout."+this.getObjectKey()+", current holder: "+holderName+", " +
                        //        "to acquired server name: "+holderRegistry.getLocalHolder().getName()+", time: "+format.format(new Date() ));
                        totalCount.addAndGet(-1);
                        throw exception;
                    }
                }
            }

            //Logger.getGlobal().info("retry to acquire lock after 50ms id: "+this.getObjectKey()+
            //        ", current holder: "+holderName+
            //        ", to acquired server name: "+holderRegistry.getLocalHolder().getName()+
            //        ", time: "+format.format(new Date()));

            Thread.sleep(50L);
        }

        //Logger.getGlobal().info("acquired  "
        //        +this.getObjectKey()
        //        +", time: "+format.format(new Date()));

        totalCount.addAndGet(-1);
        this.acquired.set(true);
        pubSub.openSubscription(this);
    }

    private CompletableFuture<Boolean> sendRequestSubscription(String holderName){
        return holderRegistry.getHolder(holderName).sendSubscribeWaiting(this);
    }

    @Override
    public void release() throws ExecutionException, InterruptedException {
        if(controller.release(generateData(this.getObjectKey()).setCurrentTime())) {
            //Logger.getGlobal().info("release successful id: "+this.getObjectKey()+
            //        "to acquired server name: "+holderRegistry.getLocalHolder().getName()+", time: "+format.format(new Date()));
            parker.releaseExceptionally();
            pubSub.invokeRetryLock(this);
            pubSub.closeSubscription(this);
            this.acquired.set(false);
        }else{
            //Logger.getGlobal().info("release failed id: " + this.getObjectKey() +
            //        "to acquired server name: " + holderRegistry.getLocalHolder().getName() + ", time: " + format.format(new Date()));
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
