package com.minepalm.syncer.core.hellobungee.entity;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class SyncReleasedLock {

    private final String objectId;

    public static class Adapter implements HelloAdapter<SyncReleasedLock>{

        @Override
        public String getIdentifier() {
            return SyncReleasedLock.class.getSimpleName();
        }

        @Override
        public void encode(ByteBuf byteBuf, SyncReleasedLock syncReleasedLock) {
            ByteBufUtils.writeString(byteBuf, syncReleasedLock.getObjectId());
        }

        @Override
        public SyncReleasedLock decode(ByteBuf byteBuf) {
            String objectId = ByteBufUtils.readString(byteBuf);
            return new SyncReleasedLock(objectId);
        }
    }
}
