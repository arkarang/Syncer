package com.minepalm.syncer.api;

/**
 * SyncToken 인터페이스는 Synced 객체의 데이터에 대한 ID를 표현하는 인터페이스입니다.
 *
 * @param <T> 데이터의 타입.
 *
 * @since 1.0
 */
public interface SyncToken<T> {

    /**
     * 지정된 데이터의 ID를 반환합니다.
     *
     * @param t ID를 생성할 데이터.
     *
     * @return 데이터의 ID.
     */
    String getObjectId(T t);

}