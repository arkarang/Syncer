package com.minepalm.syncer.api;

public interface HoldServerRegistry {

    String getLocalName();

    HoldServer getHolder(String name);

    void registerHolder(HoldServer holder);

    HoldServer getLocalHolder();
}
