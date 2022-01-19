package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetExp implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        player.setExp(data.getValues().getExp());
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }

}
