package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SetEnderChest implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getEnderChest() == null){
            return;
        }

        for(int i = 0; i < 27; i++){
            player.getInventory().setItem(i, new ItemStack(Material.AIR));
        }

        for (Map.Entry<Integer, ItemStack> entry : data.getEnderChest().getItems().entrySet()) {
            if(entry.getKey() < 27 && entry.getKey() >= 0) {
                player.getEnderChest().setItem(entry.getKey(), entry.getValue());
            }
        }
    }

}
