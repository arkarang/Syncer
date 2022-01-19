package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

public interface HeldObjectRegistry {

    <T> T getHeldObject(Class<T> clazz, String objectId);

    <T> void register(String objectId, CompletableFuture<T> future);


}
