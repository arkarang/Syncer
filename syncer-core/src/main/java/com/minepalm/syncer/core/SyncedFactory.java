package com.minepalm.syncer.core;

import com.minepalm.syncer.api.*;
import com.minepalm.syncer.core.mysql.MySQLSyncStatusDatabase;
import com.minepalm.syncer.core.mysql.MySQLSyncedController;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class SyncedFactory {

    private final ConcurrentHashMap<String, Parker> parkers = new ConcurrentHashMap<>();

    private final SyncHolderRegistry holderRegistry;
    private final SyncPubSub pubSub;
    private final MySQLSyncStatusDatabase database;

    <T> Synced<T> buildOrGet(SyncToken<T> token, T t){

        String objectId = token.getObjectId(t);

        if(!parkers.containsKey(objectId)){
            parkers.put(objectId, new Parker());
        }

        Parker parker = parkers.get(objectId);
        MySQLSyncedController controller = new MySQLSyncedController(objectId, database);
        return new DistributedSynced<>(t, token, parker, controller, holderRegistry, pubSub);
    }

}
