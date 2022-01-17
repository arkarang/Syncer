package com.minepalm.syncer.api;

public interface SyncStrategyRegistry {

    <T> void register(SyncToken<T> token, SyncFactory<T> factory);


}
