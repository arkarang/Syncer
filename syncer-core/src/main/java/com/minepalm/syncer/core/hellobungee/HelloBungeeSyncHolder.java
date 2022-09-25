package com.minepalm.syncer.core.hellobungee;

import com.minepalm.hellobungee.api.HelloSender;
import com.minepalm.syncer.api.SyncHolder;
import com.minepalm.syncer.api.SyncHolderRegistry;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.hellobungee.entity.SyncReleasedLock;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class HelloBungeeSyncHolder implements SyncHolder {

    private final ExecutorService executor;
    private final SyncHolderRegistry registry;
    private final HelloSender sender;

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public CompletableFuture<Boolean> sendSubscribeWaiting(Synced<?> synced) {
        return sender.callback(new SyncSubscription.SyncSubRequest(registry.getLocalName(), synced.getObjectKey()), SyncSubscription.SyncSubResult.class)
                .async()
                .thenApplyAsync(SyncSubscription.SyncSubResult::isAccepted, executor);
    }

    public CompletableFuture<Boolean> sendTransferHolding(Synced<?> synced) {
        return null;
    }

    @Override
    public void sendObjectReleased(Synced<?> synced) {
        sendObjectReleased(synced.getObjectKey());
    }

    @Override
    public void sendObjectReleased(String objectId) {
        sender.send(new SyncReleasedLock(objectId));
    }


}
