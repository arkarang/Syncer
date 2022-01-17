package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface SyncStrategy<T> {

    CompletableFuture<Void> onAcquiredLock(T t);

    void onSynchronizedComplete(T t);

    void onReleased(T t);

}
