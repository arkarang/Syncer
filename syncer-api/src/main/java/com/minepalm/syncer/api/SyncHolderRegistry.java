package com.minepalm.syncer.api;

public interface SyncHolderRegistry {

    SyncHolder getHolder(String name);

    void registerHolder(SyncHolder holder);

    SyncHolder getLocalHolder();
}
