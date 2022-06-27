package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetHungers implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getValues() == null){
            return;
        }

        player.setFoodLevel(data.getValues().getFoodLevel());
        player.setSaturation(data.getValues().getSaturation());
        player.setExhaustion(data.getValues().getExhaustion());
    }

}
