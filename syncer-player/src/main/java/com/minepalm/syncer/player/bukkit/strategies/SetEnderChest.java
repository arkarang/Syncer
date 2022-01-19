package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SetEnderChest implements LoadStrategy{

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        for (Map.Entry<Integer, ItemStack> entry : data.getEnderChest().getItems().entrySet()) {
            player.getEnderChest().setItem(entry.getKey(), entry.getValue());
        }
    }

}
