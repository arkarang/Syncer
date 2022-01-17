package com.minepalm.syncer.api;

public interface SyncToken<T> {

    String getObjectId(T t);

}
