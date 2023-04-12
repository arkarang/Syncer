package com.minepalm.syncer.core.mysql;

import com.minepalm.syncer.core.HoldData;

import java.util.concurrent.CompletableFuture;

interface SyncStatusStorage {

    CompletableFuture<Void> releaseAll(String server);

    CompletableFuture<Boolean> isHeldServer(String server, String objectId);

    CompletableFuture<String> getHoldingServer(String objectId);

    CompletableFuture<HoldData> getData(String objectId);

    CompletableFuture<Boolean> hold(String objectId, HoldData data, long timeoutMills);

    CompletableFuture<Void> holdUnsafe(String objectId, HoldData data);


    CompletableFuture<Boolean> release(String objectId, HoldData data);

    CompletableFuture<Void> releaseUnsafe(String objectId);

    CompletableFuture<Boolean> updateTimeout(String objectId, HoldData data);
}