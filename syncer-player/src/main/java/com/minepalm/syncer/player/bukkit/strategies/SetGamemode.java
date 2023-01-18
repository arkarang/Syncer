package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SetGamemode implements ApplyStrategy {
    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.values() == null){
            return;
        }

        player.setGameMode(GameMode.getByValue(data.values().getGamemode()));
    }
}
