package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;

public class SetLevel implements LoadStrategy{

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        player.setLevel(data.getValues().getLevel());
    }

}
