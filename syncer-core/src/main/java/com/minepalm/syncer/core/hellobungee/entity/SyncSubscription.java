package com.minepalm.syncer.core.hellobungee.entity;

import com.minepalm.library.network.api.HelloAdapter;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public final class SyncSubscription {

    public record SyncSubRequest(String sender, String objectId) {

    }

    public record SyncSubResult(boolean accepted, String requester, String objectId) {

    }


    public static class SyncSubRequestAdapter extends HelloAdapter<SyncSubRequest> {

        public SyncSubRequestAdapter() {
            super(SyncSubRequest.class.getSimpleName());
        }

        @Override
        public void encode(@NotNull ByteBuf byteBuf, SyncSubRequest request) {
            writeString(byteBuf, request.objectId());
            writeString(byteBuf, request.sender());
        }

        @NotNull
        @Override
        public SyncSubRequest decode(@NotNull ByteBuf byteBuf) {
            String objectId = readString(byteBuf);
            String sender = readString(byteBuf);
            return new SyncSubRequest(sender, objectId);
        }
    }

    public static class SyncSubResultAdapter extends HelloAdapter<SyncSubResult>{


        public SyncSubResultAdapter() {
            super(SyncSubResult.class.getSimpleName());
        }

        @Override
        public void encode(ByteBuf byteBuf, SyncSubResult result) {
            byteBuf.writeBoolean(result.accepted());
            writeString(byteBuf, result.requester());
            writeString(byteBuf, result.objectId());
        }

        @NotNull
        @Override
        public SyncSubResult decode(ByteBuf byteBuf) {
            boolean accepted = byteBuf.readBoolean();
            String requester = readString(byteBuf);
            String objectId = readString(byteBuf);

            return new SyncSubResult(accepted, requester, objectId);
        }
    }

}
