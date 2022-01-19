package com.minepalm.syncer.core.hellobungee;

import com.minepalm.hellobungee.api.HelloSender;
import com.minepalm.syncer.api.SyncHolder;
import com.minepalm.syncer.api.Synced;

import java.util.concurrent.CompletableFuture;

public class HelloBungeeSyncHolder implements SyncHolder {

    HelloSender sender;

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public <T> CompletableFuture<Synced<T>> subscribe(T t) {
        return null;
    }

    @Override
    public <T> void publish(T t) {

    }
}
