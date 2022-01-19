package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public interface LoadStrategy {

    void onPlayerLoad(Player player, PlayerData data);

    void onPlayerUnload(Player player, PlayerData data);
}
