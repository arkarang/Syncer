package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface SyncService {

    SyncStrategyRegistry getStrategyRegistry();

    SyncHolderRegistry getHolderRegistry();

    <T> Synced<T> of(T t);

}
