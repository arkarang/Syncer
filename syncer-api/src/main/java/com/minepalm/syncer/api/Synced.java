package com.minepalm.syncer.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Synced 인터페이스는 분산 환경에서 안전하게 데이터를 사용하기 위한 인터페이스입니다.
 *
 * @param <T> 동기화될 데이터의 타입.
 *
 * @since 1.0
 */
public interface Synced<T> {

    /**
     * 해당 Synced 객체가 동기화된 데이터를 반환합니다.
     *
     * @return 동기화된 데이터.
     */
    T get();

    /**
     * 해당 Synced 객체가 관리하는 데이터의 고유 식별자를 반환합니다.
     *
     * @return 데이터의 고유 식별자.
     */
    String getObjectKey();

    /**
     * 해당 Synced 객체의 잠금을 소유하고 있는 서버를 반환하는 CompletableFuture 를 반환합니다.
     *
     * @return 잠금 보유하는 서버.
     */
    CompletableFuture<HoldServer> getHoldServer();

    /**
     * 해당 Synced 객체의 데이터가 예약 중인지 여부를 반환하는 CompletableFuture 를 반환합니다.
     *
     * @return 데이터가 예약 중인지 여부.
     */
    CompletableFuture<Boolean> isHold();

    /**
     * 해당 Synced 객체의 타임아웃 시간을 추가하는 CompletableFuture를 반환합니다.
     *
     * @param timeToAdd 추가할 시간.
     *
     * @return 타임아웃 시간 추가 결과.
     */
    CompletableFuture<Boolean> extendTimeout(long timeToAdd);

    /**
     * 해당 Synced 객체의 잠금을 시도합니다.
     *
     * @param duration 잠금을 유지할 시간.
     *
     * @throws ExecutionException 예외.
     * @throws InterruptedException 예외.
     */
    void hold(Duration duration) throws ExecutionException, InterruptedException;

    /**
     * 해당 Synced 객체의 잠금을 시도합니다. timeout 초 만큼 잠금 획득을 시도합니다.
     *
     * @param duration 잠금을 유지할 시간.
     * @param timeout 타임아웃 시간.
     *
     * @throws ExecutionException 예외.
     * @throws InterruptedException 예외.
     * @throws TimeoutException 예외.
     */
    void hold(Duration duration, long timeout) throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * 해당 Synced 객체의 데이터 락을 해제합니다.
     *
     * @throws ExecutionException 예외.
     * @throws InterruptedException 예외.
     */
    void release() throws ExecutionException, InterruptedException;

    /**
     * Synced 객체의 내부 메서드에 직접 접근하기 위한 Unsafe 인터페이스를 반환합니다.
     *
     * @return Unsafe 인터페이스.
     */
    Unsafe unsafe();

    /**
     * Synced 객체의 내부 메서드에 직접 접근하기 위한 Unsafe 인터페이스입니다.
     *
     * @since 1.0
     */
    interface Unsafe{

        /**
         * 해당 Synced 객체의 락을 강제로 취득합니다.
         *
         * @throws ExecutionException 예외.
         * @throws InterruptedException 예외.
         */
        void hold() throws ExecutionException, InterruptedException;

        /**
         * 해당 Synced 객체의 락을 강제로 해지합니다.
         *
         * @throws ExecutionException 예외.
         * @throws InterruptedException 예외.
         */
        void release() throws ExecutionException, InterruptedException;

    }

}
