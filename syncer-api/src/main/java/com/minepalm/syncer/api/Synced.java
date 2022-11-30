package com.minepalm.syncer.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public interface Synced<T> {

    AtomicInteger totalCount = new AtomicInteger(0);

    T get();

    String getObjectKey();

    CompletableFuture<HoldServer> getHoldServer();

    CompletableFuture<Boolean> isHold();

    CompletableFuture<Boolean> updateTimeout(long timeToAdd);

    //CompletableFuture<Boolean> transferHold(SyncHolder holder);

    void hold(Duration duration) throws ExecutionException, InterruptedException;

    void hold(Duration duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException;

    void release() throws ExecutionException, InterruptedException;

}
