package com.minepalm.syncer.api;

public interface SyncHolderRegistry {

    String getLocalName();

    SyncHolder getHolder(String name);

    void registerHolder(SyncHolder holder);

    SyncHolder getLocalHolder();
}
