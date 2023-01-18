package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import com.minepalm.syncer.player.data.PlayerDataInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SetEquipments implements ApplyStrategy {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.inventory() == null){
            return;
        }

        PlayerDataInventory invData = data.inventory();
        PlayerInventory playerInventory = player.getInventory();

        playerInventory.setHelmet(AIR);
        playerInventory.setChestplate(AIR);
        playerInventory.setLeggings(AIR);
        playerInventory.setBoots(AIR);
        playerInventory.setItemInOffHand(AIR);

        playerInventory.setHelmet(invData.getHelmet());
        playerInventory.setChestplate(invData.getChest());
        playerInventory.setLeggings(invData.getLeggings());
        playerInventory.setBoots(invData.getBoots());
        playerInventory.setItemInOffHand(invData.getOffHand());

        player.updateInventory();
    }

}
