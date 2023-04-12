package com.minepalm.syncer.api;

/**
 * SyncPubSub 인터페이스는 Synced 객체의 구독 및 발행을 제공하는 인터페이스입니다.
 *
 * @since 1.0
 */
public interface SyncPubSub {

    /**
     * 지정된 ID를 가진 데이터의 구독을 시작합니다.
     *
     * @param objectId 구독할 데이터의 ID.
     * @param sender   구독을 시작한 클라이언트의 이름.
     * @return 구독 시작 결과.
     */
    boolean subscribe(String objectId, String sender);

    /**
     * 지정된 Synced 객체의 잠금 재시도를 요청합니다.
     *
     * @param synced 잠금을 재시도할 Synced 객체.
     * @return 재시도 요청 결과.
     */
    boolean invokeRetryLock(Synced<?> synced);

    /**
     * 지정된 Synced 객체의 구독을 시작합니다.
     *
     * @param synced 구독을 시작할 Synced 객체.
     * @return 구독 시작 결과.
     */
    boolean openSubscription(Synced<?> synced);

    /**
     * 지정된 Synced 객체의 구독을 종료합니다.
     *
     * @param synced 구독을 종료할 Synced 객체.
     * @return 구독 종료 결과.
     */
    boolean closeSubscription(Synced<?> synced);

    /**
     * 모든 데이터의 구독을 종료하고, 데이터를 보유하고 있는 서버의 연결을 모두 끊습니다.
     */
    void releaseAll();

}