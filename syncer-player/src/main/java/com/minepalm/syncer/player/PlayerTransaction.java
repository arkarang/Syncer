package com.minepalm.syncer.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PlayerTransaction {

    protected final UUID uuid;
    private final TransactionLoop loop;
    @Getter(AccessLevel.PACKAGE)
    private final Deque<Runnable> queue = new ConcurrentLinkedDeque<>();
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public CompletableFuture<Void> shutdown(){
        loop.unregister(this.uuid);
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    protected synchronized CompletableFuture<Void> addTask(Runnable run){
        CompletableFuture<Void> callee = new CompletableFuture<>();
        futures.add(callee);
        queue.add(()->{
            try{
                run.run();
                callee.complete(null);
            }catch (Throwable ex){
                callee.completeExceptionally(ex);
            }
            futures.remove(callee);
        });
        return callee;
    }

    protected synchronized <T> CompletableFuture<T> addTask(Supplier<T> run){
        CompletableFuture<T> callee = new CompletableFuture<>();
        futures.add(callee);
        queue.add(()->{
            try{
                callee.complete(run.get());
            }catch (Throwable ex){
                callee.completeExceptionally(ex);
            }
            futures.remove(callee);
        });
        return callee;
    }

    protected synchronized void runIfHasNext(){
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
    }
}
