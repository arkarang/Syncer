package com.minepalm.syncer.core;

import com.minepalm.syncer.api.HoldServer;
import com.minepalm.syncer.api.HoldServerRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class HolderRegistry implements HoldServerRegistry {

    @Getter
    private final String localName;
    ConcurrentHashMap<String, HoldServer> holders = new ConcurrentHashMap<>();

    @Override
    public HoldServer getHolder(String name) {
        return holders.get(name);
    }

    @Override
    public void registerHolder(HoldServer holder) {
        holders.put(holder.getName(), holder);
    }

    @Override
    public HoldServer getLocalHolder() {
        return getHolder(localName);
    }
}
