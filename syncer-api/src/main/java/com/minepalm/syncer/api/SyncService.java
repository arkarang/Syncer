package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface SyncService {
    SyncPubSub getPubSub();

    HoldServerRegistry getHolderRegistry();

    <T> Synced<T> of(T t);

    <T> void register(Class<T> clazz, SyncToken<T> token);

    <T> SyncToken<T> getToken(Class<T> clazz);

    CompletableFuture<Void> releaseAll();

}
