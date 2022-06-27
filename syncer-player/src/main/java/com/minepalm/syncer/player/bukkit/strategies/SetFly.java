package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.entity.Player;

public class SetFly implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getValues() == null){
            return;
        }

        boolean isFly = data.getValues().isFly();
        player.setAllowFlight(isFly);
        player.setFlying(isFly);
        EntityLiving asdf;
    }

}
