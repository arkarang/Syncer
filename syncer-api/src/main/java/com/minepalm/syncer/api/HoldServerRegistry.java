package com.minepalm.syncer.api;

/**
 * HoldServerRegistry 인터페이스는 HoldServer 객체를 등록하고 검색할 수 있는 레지스트리를 나타냅니다.
 *
 * @since 1.0
 */
public interface HoldServerRegistry {

    /**
     * 로컬 서버의 이름을 반환합니다.
     *
     * @return 로컬 서버의 이름.
     */
    String getLocalName();

    /**
     * 지정된 이름을 가진 서버의 HoldServer 객체를 반환합니다.
     *
     * @param name 검색할 서버의 이름.
     *
     * @return 검색된 HoldServer 객체.
     */
    HoldServer getHolder(String name);

    /**
     * 지정된 HoldServer 객체를 등록합니다.
     *
     * @param holder 등록할 HoldServer 객체.
     */
    void registerHolder(HoldServer holder);

    /**
     * 로컬 서버의 HoldServer 객체를 반환합니다.
     *
     * @return 로컬 서버의 HoldServer 객체.
     */
    HoldServer getLocalHolder();
}