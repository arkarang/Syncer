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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PlayerTransaction {

    protected final UUID uuid;
    @Getter(AccessLevel.PACKAGE)
    private final Deque<Runnable> queue = new ConcurrentLinkedDeque<>();
    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public CompletableFuture<Void> shutdown(){
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    protected synchronized CompletableFuture<Void> addTask(Runnable run){
        CompletableFuture<Void> callee = new CompletableFuture<>();
        futures.add(callee);
        queue.add(()->{
            synchronized (lock) {
                try {
                    run.run();
                    callee.complete(null);
                } catch (Throwable ex) {
                    callee.completeExceptionally(ex);
                }
            }
        });
        return callee;
    }

    protected synchronized <T> CompletableFuture<T> addTask(Supplier<T> run){
        CompletableFuture<T> callee = new CompletableFuture<>();
        futures.add(callee);
        queue.add(()->{
            synchronized (lock) {
                try {
                    callee.complete(run.get());
                } catch (Throwable ex) {
                    callee.completeExceptionally(ex);
                }
            }
        });
        return callee;
    }

    protected synchronized void runIfHasNext(){
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
        futures.clear();
    }
}
