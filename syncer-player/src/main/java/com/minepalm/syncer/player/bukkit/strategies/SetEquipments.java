package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SetEquipments implements LoadStrategy{

    @Override
    public void onPlayerLoad(Player player, PlayerData data) {
        PlayerDataInventory invData = data.getInventory();
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setHelmet(invData.getHelmet());
        playerInventory.setChestplate(invData.getChest());
        playerInventory.setLeggings(invData.getLeggings());
        playerInventory.setBoots(invData.getBoots());
    }

    @Override
    public void onPlayerUnload(Player player, PlayerData data) {

    }

}
