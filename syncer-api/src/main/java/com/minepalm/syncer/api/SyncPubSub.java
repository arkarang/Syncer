package com.minepalm.syncer.api;

public interface SyncPubSub {

    boolean subscribe(String objectId, String sender);

    boolean invokeRetryLock(Synced<?> synced);

    boolean openSubscription(Synced<?> synced);

    boolean closeSubscription(Synced<?> synced);

    void releaseAll();

}
