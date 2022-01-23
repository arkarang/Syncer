package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SetInventory implements LoadStrategy{

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        for(int i = 0; i < 36; i++){
            player.getInventory().setItem(0, new ItemStack(Material.AIR));
        }
        for (Map.Entry<Integer, ItemStack> entry : data.getInventory().getItems().entrySet()) {
            if(entry.getKey() < 36 && entry.getKey() >= 0) {
                player.getInventory().setItem(entry.getKey(), entry.getValue());
            }
        }
    }

}
