package com.minepalm.syncer.api.entity;

import java.util.UUID;

public record SyncLog(
    String objectId,
    UUID lockId,

    SyncAction action,

    long triedTime,
    boolean result,
    String message
) {

}
