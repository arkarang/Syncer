package com.minepalm.syncer.api;

import com.minepalm.syncer.api.entity.SyncerMode;

import java.util.concurrent.CompletableFuture;

/**
 * SyncService 인터페이스는 데이터 동기화 서비스의 메인 인터페이스입니다.
 *
 * @since 1.0
 */
public interface SyncService {


    SyncerMode getMode();

    /**
     * 데이터 구독 및 발행을 관리하는 SyncPubSub 객체를 반환합니다.
     *
     * @return SyncPubSub 객체.
     */
    SyncPubSub getPubSub();

    /**
     * HoldServer 객체 등록 및 검색을 관리하는 HoldServerRegistry 객체를 반환합니다.
     *
     * @return HoldServerRegistry 객체.
     */
    HoldServerRegistry getHolderRegistry();

    /**
     * 지정된 데이터를 Synced 객체로 래핑합니다.
     *
     * @param t Synced로 래핑할 데이터.
     *
     * @return 래핑된 Synced 객체.
     *
     * @param <T> 데이터의 타입.
     */
    <T> Synced<T> of(T t);

    /**
     * 데이터 타입과 SyncToken 객체를 등록합니다.
     *
     * @param clazz 등록할 데이터 타입.
     * @param token 등록할 SyncToken 객체.
     *
     * @param <T> 데이터의 타입.
     */
    <T> void register(Class<T> clazz, SyncToken<T> token);

    /**
     * 지정된 데이터 타입의 SyncToken 객체를 반환합니다.
     *
     * @param clazz 검색할 데이터 타입.
     *
     * @return SyncToken 객체.
     *
     * @param <T> 데이터의 타입.
     */
    <T> SyncToken<T> getToken(Class<T> clazz);

    /**
     * 모든 Synced 객체의 예약을 해제합니다.
     *
     * @return CompletableFuture.
     */
    CompletableFuture<Void> releaseAll();

}