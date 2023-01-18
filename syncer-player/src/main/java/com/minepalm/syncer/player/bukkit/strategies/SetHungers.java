package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.entity.Player;

public class SetHungers implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.values() == null){
            return;
        }

        player.setFoodLevel(data.values().getFoodLevel());
        player.setSaturation(data.values().getSaturation());
        player.setExhaustion(data.values().getExhaustion());
    }

}
