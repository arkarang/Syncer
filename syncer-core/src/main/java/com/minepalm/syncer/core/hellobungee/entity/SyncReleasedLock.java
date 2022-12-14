package com.minepalm.syncer.core.hellobungee.entity;

import com.minepalm.library.network.api.HelloAdapter;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncReleasedLock(String objectId) {

    public static class Adapter extends HelloAdapter<SyncReleasedLock> {


        public Adapter() {
            super(SyncReleasedLock.class.getSimpleName());
        }

        @Override
        public void encode(@NotNull ByteBuf byteBuf, SyncReleasedLock syncReleasedLock) {
            writeString(byteBuf, syncReleasedLock.objectId());
        }

        @NotNull
        @Override
        public SyncReleasedLock decode(@NotNull ByteBuf byteBuf) {
            String objectId = readString(byteBuf);
            return new SyncReleasedLock(objectId);
        }
    }
}
