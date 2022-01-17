package com.minepalm.syncer.core;

import com.minepalm.syncer.api.SyncHolderRegistry;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.SyncStrategyRegistry;

public class Syncer implements SyncService {
    @Override
    public SyncStrategyRegistry getStrategyRegistry() {
        return null;
    }

    @Override
    public SyncHolderRegistry getHolderRegistry() {
        return null;
    }
}
