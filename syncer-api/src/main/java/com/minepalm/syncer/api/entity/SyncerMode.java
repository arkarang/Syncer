package com.minepalm.syncer.api.entity;

/**
 * SyncerMode 는 데이터 동기화 서비스 구현을 무엇을 사용하는지 나타내는 열거형입니다.
 * LOCAL 모드의 경우 하나의 로컬 애플리케이션 서버가 데이터의 동기화 트랜젝션을 관리하게 됩니다.
 * REDIS 모드의 경우 Redis의 PubSub를 통해 데이터를 동기화합니다.
 * MYSQL 모드의 경우 MySQL의 테이블을 통해 낙관적 락을 기반으로 데이터를 동기화하게 됩니다.
 *
 * @since 1.0
 */
public enum SyncerMode {
    LOCAL, REDIS, MYSQL

}
