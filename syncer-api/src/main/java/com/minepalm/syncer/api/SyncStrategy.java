package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface SyncStrategy<T> {

    CompletableFuture<Void> onSynchronizedComplete(T t);

    void onReleased(T t);

}
