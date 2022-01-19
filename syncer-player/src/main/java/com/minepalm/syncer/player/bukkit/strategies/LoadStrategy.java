package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public interface LoadStrategy {

    void applyPlayer(Player player, PlayerData data);

}
