package com.minepalm.syncer.core.hellobungee.entity;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class SyncTransferLock {

    private final String objectId;

    public static class Adapter implements HelloAdapter<SyncTransferLock>{

        @Override
        public String getIdentifier() {
            return SyncTransferLock.class.getSimpleName();
        }

        @Override
        public void encode(ByteBuf byteBuf, SyncTransferLock syncTransferLock) {
            ByteBufUtils.writeString(byteBuf, syncTransferLock.getObjectId());
        }

        @Override
        public SyncTransferLock decode(ByteBuf byteBuf) {
            String objectId = ByteBufUtils.readString(byteBuf);
            return new SyncTransferLock(objectId);
        }

    }

}
