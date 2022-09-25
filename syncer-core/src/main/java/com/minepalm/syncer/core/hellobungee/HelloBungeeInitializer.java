package com.minepalm.syncer.core.hellobungee;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.core.hellobungee.entity.SyncReleasedLock;
import com.minepalm.syncer.core.hellobungee.entity.SyncSubscription;
import com.minepalm.syncer.core.hellobungee.executors.SyncReleaseLockExecutor;
import com.minepalm.syncer.core.hellobungee.executors.SyncSubscriptionCallback;

import java.util.concurrent.Executors;

public class HelloBungeeInitializer {

    public static void initialize(SyncService service, HelloEveryone networkModule){
        networkModule.all().forEach(sender-> service.getHolderRegistry().registerHolder(
                new HelloBungeeSyncHolder(Executors.newCachedThreadPool(), service.getHolderRegistry(), sender)));
        networkModule.getCallbackService().registerTransformer(new SyncSubscriptionCallback(service.getPubSub()));
        networkModule.getHandler().registerExecutor(new SyncReleaseLockExecutor((Syncer)service));
        networkModule.getGateway().registerAdapter(new SyncReleasedLock.Adapter());
        networkModule.getGateway().registerAdapter(new SyncSubscription.SyncSubRequestAdapter());
        networkModule.getGateway().registerAdapter(new SyncSubscription.SyncSubResultAdapter());
    }
}
