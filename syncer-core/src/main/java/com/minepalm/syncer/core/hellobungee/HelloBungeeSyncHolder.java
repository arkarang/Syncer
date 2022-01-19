package com.minepalm.syncer.core.hellobungee;

import com.minepalm.hellobungee.api.HelloSender;
import com.minepalm.syncer.api.SyncHolder;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.hellobungee.entity.SyncReleasedLock;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class HelloBungeeSyncHolder implements SyncHolder {

    private final HelloSender sender;

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public CompletableFuture<Boolean> sendSubscribeWaiting(Synced<?> synced) {
        return sender.callback(new SyncSubscription.SyncSubRequest(sender.getName(), synced.getObjectKey()), SyncSubscription.SyncSubResult.class)
                .async()
                .thenApply(SyncSubscription.SyncSubResult::isAccepted);
    }

    public CompletableFuture<Boolean> sendTransferHolding(Synced<?> synced) {
        return null;
    }

    @Override
    public void sendObjectReleased(Synced<?> synced) {
        sender.send(new SyncReleasedLock(synced.getObjectKey()));
    }


}
