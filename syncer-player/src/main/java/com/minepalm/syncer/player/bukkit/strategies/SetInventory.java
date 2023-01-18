package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SetInventory implements ApplyStrategy {

    private final Logger logger;
    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.inventory() == null){
            logger.warning("player "+player.getName()+"("+player.getUniqueId()+") inventory is null");
            return;
        }

        for(int i = 0; i < 36; i++){
            player.getInventory().setItem(i, new ItemStack(Material.AIR));
        }
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));

        for (Map.Entry<Integer, ItemStack> entry : data.inventory().getItems().entrySet()) {
            if(entry.getKey() < 36 && entry.getKey() >= 0) {
                player.getInventory().setItem(entry.getKey(), entry.getValue());
            }
        }

        player.getInventory().setHelmet(data.inventory().getHelmet());
        player.getInventory().setChestplate(data.inventory().getChest());
        player.getInventory().setLeggings(data.inventory().getLeggings());
        player.getInventory().setBoots(data.inventory().getBoots());

        if(data.values() == null){
            return;
        }

        player.getInventory().setHeldItemSlot(data.values().getHeldSlot());
        player.updateInventory();

    }

}
