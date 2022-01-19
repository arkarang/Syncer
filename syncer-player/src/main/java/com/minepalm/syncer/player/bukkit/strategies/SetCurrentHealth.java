package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetCurrentHealth implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        player.setHealth(data.getValues().getHealth());
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }
}
