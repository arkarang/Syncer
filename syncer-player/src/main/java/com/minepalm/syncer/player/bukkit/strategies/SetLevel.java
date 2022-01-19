package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetLevel implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        player.setLevel(data.getValues().getLevel());
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }

}
