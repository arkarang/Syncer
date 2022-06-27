package com.minepalm.syncer.player.bukkit;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalLocks {

    private static final ConcurrentHashMap<UUID, Lock> locks = new ConcurrentHashMap<>();

    public static void add(UUID uuid){
        locks.putIfAbsent(uuid, new ReentrantLock());
    }

    public static void lock(UUID uuid){
        locks.putIfAbsent(uuid, new ReentrantLock());
        locks.get(uuid).lock();
    }

    public static void unlock(UUID uuid){
        if(locks.containsKey(uuid)){
            locks.get(uuid).unlock();
        }
    }

    public static void remove(UUID uuid){
        locks.remove(uuid);
    }
}
