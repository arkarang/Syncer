package com.minepalm.syncer.core.mysql;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class MySQLSyncedController {

    private final String objectId;
    private final String currentServer;
    protected final MySQLSyncStatusDatabase database;

    public CompletableFuture<String> getHoldServer() {
        return database.getHoldingServer(objectId);
    }

    public CompletableFuture<Boolean> isHold() {
        return database.isHeldServer(currentServer, objectId);
    }

    public CompletableFuture<Boolean> tryHoldAsync(long holdingDuration){
        long currentTime = System.currentTimeMillis();
        return database.hold(objectId, currentServer, currentTime, holdingDuration);
    }

    public boolean tryHold(long holdingDuration) {
        return tryHoldAsync(holdingDuration).join();
    }

    public void holdUnsafe(long time) {
        database.holdUnsafe(objectId, currentServer, time).join();
    }

    public CompletableFuture<Void> holdUnsafeAsync(long time){
        return database.holdUnsafe(objectId, currentServer, time);
    }

    public CompletableFuture<Boolean> releaseAsync(long time){
        return database.release(objectId, currentServer, time);
    }

    public boolean release(long time) {
        return database.release(objectId, currentServer, time).join();
    }

    public CompletableFuture<Void> releaseUnsafeAsync(){
        return database.releaseUnsafe(objectId);
    }

    public void releaseUnsafe() {
        database.releaseUnsafe(objectId).join();
    }

    public CompletableFuture<Boolean> setTimeout(long time){
        return database.setTimeout(objectId, currentServer, time);
    }

    public CompletableFuture<Boolean> updateTimeout(long time){
        return database.updateTimeout(objectId, currentServer , time);
    }
}
