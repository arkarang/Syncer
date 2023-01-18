package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.entity.Player;

public class SetFly implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.values() == null){
            return;
        }

        boolean isFly;
        if(data.values().getGamemode() == 1){
            isFly = true;
        }else{
            isFly = data.values().isFly();
        }

        player.setAllowFlight(isFly);
        player.setFlying(isFly);
    }

}
