package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface SyncHolder {

    String getName();

    <T> CompletableFuture<Synced<T>> subscribe(T t);

    <T> void publish(T t);
}
