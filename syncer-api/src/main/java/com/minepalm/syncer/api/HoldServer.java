package com.minepalm.syncer.api;

import java.util.concurrent.CompletableFuture;

/**
 * HoldServer 인터페이스는 동기화된 데이터를 보유할 수 있는 서버를 나타냅니다.
 *
 * @since 1.0
 */
public interface HoldServer {

    /**
     * 이 서버의 이름을 반환합니다.
     *
     * @return 이 서버의 이름.
     */
    String getName();

    /**
     * 지정된 데이터를 예약하기 위해 서버에 메시지를 보냅니다.
     *
     * @param synced 예약할 데이터.
     *
     * @return 데이터 예약 결과를 반환하는 CompletableFuture.
     */
    CompletableFuture<Boolean> sendSubscribeWaiting(Synced<?> synced);

    /**
     * 지정된 데이터의 예약을 해제합니다.
     *
     * @param synced 예약을 해제할 데이터.
     */
    void sendObjectReleased(Synced<?> synced);

    /**
     * 지정된 데이터의 예약을 해제합니다.
     *
     * @param objectId 예약을 해제할 데이터의 ID.
     */
    void sendObjectReleased(String objectId);

}