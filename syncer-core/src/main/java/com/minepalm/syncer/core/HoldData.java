package com.minepalm.syncer.core;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class HoldData {

    private final String objectId;
    private final String server;
    private final long time;

    public HoldData setObjectId(String objectId){
        return new HoldData(objectId, this.server, this.time);
    }

    public HoldData setServerName(String server){
        return new HoldData(this.objectId, server, this.time);
    }

    public HoldData setTime(long time){
        return new HoldData(this.objectId, this.server, time);
    }

    public HoldData setCurrentTime(){
        return new HoldData(this.objectId, this.server, System.currentTimeMillis());
    }

    public HoldData setTimeoutAfter(long mills){
        return new HoldData(this.objectId, this.server, System.currentTimeMillis() + mills);
    }

}
