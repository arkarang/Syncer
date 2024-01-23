package com.minepalm.syncer.player.bukkit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerData {

    private final UUID uuid;
    @Nullable
    private final PlayerDataValues values;
    @Nullable
    private final PlayerDataInventory inventory;
    @Nullable
    private final PlayerDataEnderChest enderChest;
    @Nullable
    private final PlayerDataPotion potionEffects;

}
