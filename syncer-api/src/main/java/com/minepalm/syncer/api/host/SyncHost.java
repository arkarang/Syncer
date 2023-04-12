package com.minepalm.syncer.api.host;

import com.minepalm.syncer.api.SyncPubSub;

/**
 * SyncHost 는 데이터 동기화 서비스를 제공하는 호스트를 나타내는 인터페이스입니다.
 * Syncer 의 애플리케이션 호스트는 다음과 같은 역할을 수행합니다.
 *  - 데이터 동기화 요청을 받고, 데이터 동기화 서비스를 제공합니다.
 *  - 데이터 동기화 요청을 받은 순서에 맞게 락의 순서를 제공하여, 락 경쟁을 해소시킵니다.
 *  - 데이터 동기화 요청을 받은 순서에 맞게 데이터 동기화를 수행합니다.
 *  - 데이터 동기화 락을 지속적으로 모니터링 하여, 로그를 남깁니다.
 */
public interface SyncHost {

    SyncPubSub getPubSub();

    SyncLogManager logManager();

}
