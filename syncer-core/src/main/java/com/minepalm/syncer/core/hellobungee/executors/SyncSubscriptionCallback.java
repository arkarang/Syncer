package com.minepalm.syncer.core.hellobungee.executors;

import com.minepalm.hellobungee.api.CallbackTransformer;
import com.minepalm.syncer.api.SyncPubSub;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;
import lombok.RequiredArgsConstructor;

import java.util.logging.LogManager;

@RequiredArgsConstructor
public class SyncSubscriptionCallback implements CallbackTransformer<SyncSubscription.SyncSubRequest, SyncSubscription.SyncSubResult> {

    private final SyncPubSub syncPubSub;

    @Override
    public String getIdentifier() {
        return SyncSubscription.SyncSubRequest.class.getSimpleName();
    }

    @Override
    public SyncSubscription.SyncSubResult transform(SyncSubscription.SyncSubRequest request) {
        boolean result = syncPubSub.subscribe(request.getObjectId(), request.getSender());
        return new SyncSubscription.SyncSubResult(result, request.getSender(), request.getObjectId());
    }
}
