package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface HoldServer {

    String getName();

    CompletableFuture<Boolean> sendSubscribeWaiting(Synced<?> synced);

    //CompletableFuture<Boolean> sendTransferHolding(Synced<?> synced);

    void sendObjectReleased(Synced<?> synced);

    void sendObjectReleased(String objectId);

}
