package com.minepalm.syncer.core;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.syncer.api.*;
import com.minepalm.syncer.core.hellobungee.HelloBungeeInitializer;
import com.minepalm.syncer.core.hellobungee.HelloBungeePubSubs;
import com.minepalm.syncer.core.mysql.MySQLSyncStatusDatabase;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Syncer implements SyncService {

    @Getter
    SyncPubSub pubSub;
    @Getter
    SyncHolderRegistry holderRegistry;
    MySQLSyncStatusDatabase database;
    SyncedFactory factory;

    private final ConcurrentHashMap<Class<?>, SyncToken<?>> tokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Synced<?>> syncedObjects = new ConcurrentHashMap<>();

    public Syncer(MySQLDatabase database, HelloEveryone network){
        this.holderRegistry = new HolderRegistry(network.getName());
        this.pubSub = new HelloBungeePubSubs(this.holderRegistry);
        this.database = new MySQLSyncStatusDatabase("syncer_status", database);
        this.factory = new SyncedFactory(holderRegistry, pubSub, this.database);

        HelloBungeeInitializer.initialize(this, network);
        this.database.init();
    }

    public void initProcedures(){
        this.database.initProcedures();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Synced<T> of(T t) {
        SyncToken<T> token = (SyncToken<T>)getToken(t.getClass());

        if(token == null){
            throw new IllegalArgumentException("token of "+t.getClass().getSimpleName()+" is not registered");
        }

        String objectId = token.getObjectId(t);

        if(syncedObjects.containsKey(objectId)){
            return (Synced<T>)syncedObjects.get(objectId);
        }

        Synced<T> synced = factory.buildOrGet(token, t);
        syncedObjects.put(objectId, synced);

        return synced;
    }

    @Override
    public <T> void register(Class<T> clazz, SyncToken<T> token) {
        tokens.put(clazz, token);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> SyncToken<T> getToken(Class<T> clazz) {
        return (SyncToken<T>)tokens.get(clazz);
    }

    @Override
    public CompletableFuture<Void> releaseAll() {
        this.pubSub.releaseAll();
        return database.releaseAll(this.holderRegistry.getLocalHolder().getName());
    }

    public synchronized void signalReleaseLock(String objectId){
        if(syncedObjects.containsKey(objectId)) {
            ((DistributedSynced<?>) syncedObjects.get(objectId)).releasePark();
        }
    }

}
