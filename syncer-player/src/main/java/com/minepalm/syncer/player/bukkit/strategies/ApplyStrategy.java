package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.entity.Player;

public interface ApplyStrategy {

    void applyPlayer(Player player, PlayerData data);

}
