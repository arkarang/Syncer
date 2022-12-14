package com.minepalm.syncer.core.hellobungee.executors;

import com.minepalm.library.network.api.CallbackTransformer;
import com.minepalm.syncer.api.SyncPubSub;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SyncSubscriptionCallback implements CallbackTransformer<SyncSubscription.SyncSubRequest, SyncSubscription.SyncSubResult> {

    private final SyncPubSub syncPubSub;

    @NotNull
    @Override
    public String getIdentifier() {
        return SyncSubscription.SyncSubRequest.class.getSimpleName();
    }

    @Override
    public SyncSubscription.SyncSubResult transform(SyncSubscription.SyncSubRequest request) {
        boolean result = syncPubSub.subscribe(request.objectId(), request.sender());
        return new SyncSubscription.SyncSubResult(result, request.sender(), request.objectId());
    }
}
