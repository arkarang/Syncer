package com.minepalm.syncer.player;

import com.minepalm.syncer.player.bukkit.PlayerData;
import lombok.Data;

import java.util.UUID;

@Data
public class ErrorReport {

    public final UUID uuid;
    public final String server;

    public final PlayerData playerData;

    public final String description;
    public final Throwable exception;
    public final long time;
}
