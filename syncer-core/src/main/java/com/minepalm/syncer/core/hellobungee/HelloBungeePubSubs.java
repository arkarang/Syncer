package com.minepalm.syncer.core.hellobungee;

import com.minepalm.syncer.api.HoldServer;
import com.minepalm.syncer.api.HoldServerRegistry;
import com.minepalm.syncer.api.SyncPubSub;
import com.minepalm.syncer.api.Synced;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class HelloBungeePubSubs implements SyncPubSub {

    private final ConcurrentHashMap<String, SubscriptionsList> subscribes = new ConcurrentHashMap<>();
    private final HoldServerRegistry holderRegistry;

    protected static class SubscriptionsList {

        private final List<String> list = new ArrayList<>();

        protected synchronized void add(String str){
            list.add(str);
        }

        protected synchronized List<String> getAll(){
            return new ArrayList<>(list);
        }
    }

    @Override
    public boolean subscribe(String objectId, String sender){
        if(subscribes.containsKey(objectId)){
            subscribes.get(objectId).add(sender);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean invokeRetryLock(Synced<?> synced){
        String objectId = synced.getObjectKey();

        if(subscribes.containsKey(objectId)){
            List<String> names = subscribes.get(objectId).getAll();
            List<HoldServer> holders = new ArrayList<>();
            for (String name : names) {
                HoldServer sender = holderRegistry.getHolder(name);
                if(sender != null){
                    holders.add(sender);
                }
            }
            holders.forEach(holder->holder.sendObjectReleased(synced));
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean openSubscription(Synced<?> synced) {
        String objectId = synced.getObjectKey();

        if(subscribes.containsKey(objectId)){
            return false;
        }else{
            subscribes.put(objectId, new SubscriptionsList());
            return true;
        }
    }

    @Override
    public boolean closeSubscription(Synced<?> synced) {
        String objectId = synced.getObjectKey();

        if(subscribes.containsKey(objectId)){
            subscribes.remove(objectId);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void releaseAll() {
        for (String objectId : subscribes.keySet()) {
            List<String> names = subscribes.get(objectId).getAll();
            List<HoldServer> holders = new ArrayList<>();
            for (String name : names) {
                HoldServer sender = holderRegistry.getHolder(name);
                if(sender != null){
                    holders.add(sender);
                }
            }
            holders.forEach(holder->holder.sendObjectReleased(objectId));
        }
    }

}
