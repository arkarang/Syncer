package com.minepalm.syncer.player.bukkit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerData {

    private final UUID uuid;
    private final PlayerDataValues values;
    private final PlayerDataInventory inventory;
    private final PlayerDataEnderChest enderChest;

}
