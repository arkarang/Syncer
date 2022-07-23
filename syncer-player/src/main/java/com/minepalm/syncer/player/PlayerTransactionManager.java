package com.minepalm.syncer.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PlayerTransactionManager {

    /*
    private final TransactionLoop loop;

    public CompletableFuture<Void> commit(UUID uuid, Runnable run){
        register(uuid);
        return loop.transactions.get(uuid).addTask(run);

    }

    public <T> CompletableFuture<T> commit(UUID uuid, Supplier<T> sup){
        register(uuid);
        return loop.transactions.get(uuid).addTask(sup);
    }

    public synchronized void register(UUID uuid){
        if(!loop.transactions.containsKey(uuid)){
            loop.register(uuid, new PlayerTransaction(uuid, loop));
        }
    }

    public synchronized void unregister(UUID uuid){
        loop.unregister(uuid);
    }

     */

    private final ExecutorService executor;
    private final Cache<UUID, CompletableFuture<?>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public synchronized CompletableFuture<Void> commit(UUID uuid, Runnable run){
        CompletableFuture<?> beforeFuture = cache.getIfPresent(uuid);
        if(beforeFuture == null){
            beforeFuture = CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> afterFuture = beforeFuture.thenRunAsync(run, executor);
        cache.put(uuid, afterFuture);
        return afterFuture;
    }

    public synchronized <T> CompletableFuture<T> commit(UUID uuid, Supplier<T> sup){
        CompletableFuture<?> beforeFuture = cache.getIfPresent(uuid);
        if(beforeFuture == null){
            beforeFuture = CompletableFuture.completedFuture(null);
        }
        CompletableFuture<T> afterFuture = beforeFuture.thenApplyAsync(ignored -> sup.get(), executor);
        cache.put(uuid, afterFuture);
        return afterFuture;
    }

}
