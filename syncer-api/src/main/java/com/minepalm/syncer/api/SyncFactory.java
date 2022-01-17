package com.minepalm.syncer.api;

public interface SyncFactory<T> {

    Synced<T> get(T t);
}
