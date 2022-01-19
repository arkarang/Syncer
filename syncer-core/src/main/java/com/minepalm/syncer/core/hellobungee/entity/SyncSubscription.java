package com.minepalm.syncer.core.hellobungee.entity;

import com.minepalm.hellobungee.api.HelloAdapter;
import com.minepalm.hellobungee.netty.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SyncSubscription {

    @RequiredArgsConstructor
    @Data
    public static class SyncSubRequest {

        private final String sender;
        private final String objectId;

    }

    @RequiredArgsConstructor
    @Data
    public static class SyncSubResult {

        private final boolean accepted;
        private final String requester;
        private final String objectId;

    }


    public static class SyncSubRequestAdapter implements HelloAdapter<SyncSubRequest> {

        @Override
        public String getIdentifier() {
            return SyncSubRequest.class.getSimpleName();
        }

        @Override
        public void encode(ByteBuf byteBuf, SyncSubRequest request) {
            ByteBufUtils.writeString(byteBuf, request.getObjectId());
            ByteBufUtils.writeString(byteBuf, request.getSender());
        }

        @Override
        public SyncSubRequest decode(ByteBuf byteBuf) {
            String objectId = ByteBufUtils.readString(byteBuf);
            String sender = ByteBufUtils.readString(byteBuf);
            return new SyncSubRequest(objectId, sender);
        }
    }

    public static class SyncSubResultAdapter implements HelloAdapter<SyncSubResult>{

        @Override
        public String getIdentifier() {
            return SyncSubResult.class.getSimpleName();
        }

        @Override
        public void encode(ByteBuf byteBuf, SyncSubResult result) {
            byteBuf.writeBoolean(result.isAccepted());
            ByteBufUtils.writeString(byteBuf, result.getObjectId());
            ByteBufUtils.writeString(byteBuf, result.getObjectId());
        }

        @Override
        public SyncSubResult decode(ByteBuf byteBuf) {
            boolean accepted = byteBuf.readBoolean();
            String requester = ByteBufUtils.readString(byteBuf);
            String objectId = ByteBufUtils.readString(byteBuf);

            return new SyncSubResult(accepted, requester, objectId);
        }
    }

}
