package com.minepalm.syncer.core.hellobungee.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;

public class SyncSubscriptionExecutor {

    public static class SyncSubRequestExecutor implements HelloExecutor<SyncSubscription.SyncSubRequest> {

        

        @Override
        public String getIdentifier() {
            return SyncSubscription.SyncSubRequest.class.getSimpleName();
        }

        @Override
        public void executeReceived(SyncSubscription.SyncSubRequest request) {

        }

    }

    public static class SyncPubRequestExecutor implements HelloExecutor<SyncSubscription.SyncSubResult> {

        @Override
        public String getIdentifier() {
            return SyncSubscription.SyncSubResult.class.getSimpleName();
        }

        @Override
        public void executeReceived(SyncSubscription.SyncSubResult result) {

        }

    }
}
