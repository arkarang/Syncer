package com.minepalm.syncer.api;

public interface SyncService {

    SyncStrategyRegistry getStrategyRegistry();

    SyncHolderRegistry getHolderRegistry();

    <T> Synced<T> of(T t);

}
