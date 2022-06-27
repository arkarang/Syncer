package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetHealthScale implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getValues() == null){
            return;
        }

        player.setHealthScaled(true);
        player.setHealthScale(data.getValues().getHealthScale());
    }
}
