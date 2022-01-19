package com.minepalm.syncer.api;

public interface SyncPubSub {

    boolean subscribe(String objectId, String sender);

    boolean invokeRetryLock(String objectId);

    boolean openSubscription(String objectId);

    boolean closeSubscription(String objectId);

}
