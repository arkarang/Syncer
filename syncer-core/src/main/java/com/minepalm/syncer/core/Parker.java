package com.minepalm.syncer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Parker {

    private CompletableFuture<Void> future = new CompletableFuture<>();

    public void park(long mills) throws TimeoutException {
        try {
            future.get(mills, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | ExecutionException ignored){

        }
    }

    public synchronized void release(){
        future.complete(null);
        this.future = new CompletableFuture<>();
    }

    public synchronized void releaseExceptionally(){
        future.completeExceptionally(new InterruptedException());
        this.future = new CompletableFuture<>();
    }
}
