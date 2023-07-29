package com.minepalm.syncer.bootstrap;

import com.minepalm.syncer.api.SyncToken;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public class UUIDHolder implements SyncToken<UUID> {


    @Override
    public String getObjectId(UUID uuid) {
        return uuid.toString();
    }
}
