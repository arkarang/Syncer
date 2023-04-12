package com.minepalm.syncer.api;

import com.minepalm.syncer.api.entity.SyncerMode;

/**
 * Syncer의 일반적 설정을 관리하는 인터페이스입니다.
 */
public interface SyncerConfiguration {

    /**
     * Syncer의 동기화 모드를 반환합니다.
     * @see SyncerMode
     */
    SyncerMode getMode();

    /**
     * Syncer의 로컬 동기화 모드 세부 설정을 관리하는 인터페이스입니다.
     * 기본적으로 Syncer 의 동기화는 Publish-Subscribe 방식을 사용합니다.
     */
    interface Local extends SyncerConfiguration {

        /**
         * Syncer의 Publish-Subscribe 를 담당하는 서비스 호스트를 반환합니다.
         * @return PalmNetwork의 호스트 명칭 ( ex. proxy )
         */
        String getServiceHost();

        /**
         * 이 애플리케이션이 서비스 호스트인지를 반환합니다.
         */
        boolean isServiceHost();

        /**
         * Syncer의 Publish-Subscribe 최대 지연 시간을 반환합니다.
         * @return 최대 지연 시간 (단위: ms)
         */
        long getRetryDelay();

        /**
         * Syncer의 Publish-Subscribe 최대 재시도 횟수를 반환합니다.
         */
        int getMaximumRetryCount();

    }

    interface Redis extends Local {

        /**
         * Syncer의 Redis 호스트를 반환합니다.
         * @return PalmLibrary-Database 의 데이터베이스 명칭 ( ex. redis-default )
         */
        String gerRedisHost();

    }


    interface MySQL {

        /**
         * Syncer의 MySQL 호스트를 반환합니다.
         * @return PalmLibrary-Database 의 데이터베이스 명칭 ( ex. mysql-default )
         */
        String getMySQLHost();

        /**
         * Syncer의 MySQL 테이블 명칭을 반환합니다.
         * @return MySQL 테이블 명칭
         */
        String getTableName();

    }

}
