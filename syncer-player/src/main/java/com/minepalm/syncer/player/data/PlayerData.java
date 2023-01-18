package com.minepalm.syncer.player.data;

import com.minepalm.syncer.player.bukkit.PlayerDataValues;

import javax.annotation.Nullable;
import java.util.UUID;

public record PlayerData(
        UUID uuid,
        @Nullable PlayerDataValues values,
        @Nullable PlayerDataInventory inventory,
        @Nullable PlayerDataEnderChest enderChest,

        @Nullable PlayerDataPotion potions) {

}
