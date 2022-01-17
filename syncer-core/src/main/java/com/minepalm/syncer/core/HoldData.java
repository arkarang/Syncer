package com.minepalm.syncer.core;

import com.minepalm.syncer.api.SyncStage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class HoldData {

    private final String objectId;
    private final String proxy;
    private final String server;
    private final SyncStage stage;
    private final long time;

    public HoldData setObjectId(String objectId){
        return new HoldData(objectId, this.proxy, this.server, this.stage, this.time);
    }

    public HoldData setProxyName(String proxy){
        return new HoldData(this.objectId, proxy, this.server, this.stage, this.time);
    }

    public HoldData setServerName(String server){
        return new HoldData(this.objectId, this.proxy, server, this.stage, this.time);
    }

    public HoldData serStage(SyncStage stage){
        return new HoldData(this.objectId, this.proxy, this.server, stage, this.time);
    }

    public HoldData setTime(long time){
        return new HoldData(this.objectId, this.proxy, this.server, this.stage, time);
    }

}
