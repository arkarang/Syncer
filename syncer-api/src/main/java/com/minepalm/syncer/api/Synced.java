package com.minepalm.syncer.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public interface Synced<T> {

    T get();

    String getObjectKey();

    CompletableFuture<SyncStage> getStage();

    CompletableFuture<SyncHolder> getHoldProxy();

    CompletableFuture<SyncHolder> getHoldServer();

    CompletableFuture<Boolean> isHold();

    CompletableFuture<Void> transferHold(SyncHolder holder);

    void hold() throws ExecutionException, InterruptedException;

    void hold(Duration duration, long time) throws ExecutionException, InterruptedException, TimeoutException;

    CompletableFuture<Void> releaseAsync();

    void release() throws ExecutionException, InterruptedException;

    CompletableFuture<Void> waitHoldingAsync(ExecutorService service);


}
