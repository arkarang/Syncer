package com.minepalm.syncer.core;

import com.minepalm.syncer.api.SyncHolder;
import com.minepalm.syncer.api.SyncHolderRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class HolderRegistry implements SyncHolderRegistry {

    @Getter
    private final String localName;
    ConcurrentHashMap<String, SyncHolder> holders = new ConcurrentHashMap<>();

    @Override
    public SyncHolder getHolder(String name) {
        return holders.get(name);
    }

    @Override
    public void registerHolder(SyncHolder holder) {
        holders.put(holder.getName(), holder);
    }

    @Override
    public SyncHolder getLocalHolder() {
        return getHolder(localName);
    }
}
