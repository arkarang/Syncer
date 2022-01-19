package com.minepalm.syncer.core.hellobungee;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.api.HelloSender;
import com.minepalm.syncer.api.SyncPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HelloBungeePubSubs implements SyncPubSub {

    private final ConcurrentHashMap<String, SubscriptionsList> subscribes = new ConcurrentHashMap<>();
    private HelloEveryone networkModule;

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

    /**
     *
     * @param objectId
     */
    @Override
    public boolean invokeRetryLock(String objectId){
        if(subscribes.containsKey(objectId)){
            List<String> names = subscribes.get(objectId).getAll();
            List<HelloSender> senders = new ArrayList<>();
            for (String name : names) {
                HelloSender sender = networkModule.sender(name);
                if(sender != null){
                    senders.add(sender);
                }
            }
            for (HelloSender sender : senders) {
                sender.send();
            }
        }
    }

    @Override
    public boolean openSubscription(String objectId) {
        return false;
    }

    @Override
    public boolean closeSubscription(String objectId) {
        return false;
    }

}
