package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.entity.Player;

public class SetLevel implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.values() == null){
            return;
        }

        player.setLevel(data.values().getLevel());
    }

}
