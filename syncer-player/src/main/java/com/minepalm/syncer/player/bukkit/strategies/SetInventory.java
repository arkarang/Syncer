package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SetInventory implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        for(int i = 0; i < 36; i++){
            player.getInventory().setItem(0, new ItemStack(Material.AIR));
        }
        data.getInventory().getItems().forEach((slot, item)-> player.getInventory().setItem(slot, item));
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }
}
