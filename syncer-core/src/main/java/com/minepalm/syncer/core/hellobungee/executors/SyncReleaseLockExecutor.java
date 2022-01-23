package com.minepalm.syncer.core.hellobungee.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.core.hellobungee.entity.SyncReleasedLock;
import lombok.RequiredArgsConstructor;

import java.util.logging.LogManager;

@RequiredArgsConstructor
public class SyncReleaseLockExecutor implements HelloExecutor<SyncReleasedLock> {

    private final Syncer service;

    @Override
    public String getIdentifier() {
        return SyncReleasedLock.class.getSimpleName();
    }

    @Override
    public void executeReceived(SyncReleasedLock signal) {
        service.signalReleaseLock(signal.getObjectId());
    }

}
