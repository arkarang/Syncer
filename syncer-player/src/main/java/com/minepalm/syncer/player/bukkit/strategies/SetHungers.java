package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetHungers implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        player.setFoodLevel(data.getValues().getFoodLevel());
        player.setSaturation(data.getValues().getSaturation());
        player.setExhaustion(data.getValues().getExhaustion());
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }

}
