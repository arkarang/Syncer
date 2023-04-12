package com.minepalm.syncer.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class Parker {

    private AtomicReference<CompletableFuture<Void>> future = new AtomicReference<>(new CompletableFuture<>());

    public void park(long mills) throws TimeoutException {
        try {
            future.get().get(mills, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            // 적절한 로깅이나 예외 처리를 추가할 수 있습니다.
            e.printStackTrace();
        } catch (TimeoutException e) {
            throw e;
        }
    }

    public void release() {
        CompletableFuture<Void> oldFuture;
        do {
            oldFuture = future.get();
        } while (!future.compareAndSet(oldFuture, new CompletableFuture<>()));

        oldFuture.complete(null);
    }

    public void releaseExceptionally() {
        CompletableFuture<Void> oldFuture;
        do {
            oldFuture = future.get();
        } while (!future.compareAndSet(oldFuture, new CompletableFuture<>()));

        oldFuture.completeExceptionally(new InterruptedException());
    }
}
