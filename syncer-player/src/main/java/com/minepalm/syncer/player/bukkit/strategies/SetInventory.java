package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
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

        if(data.getInventory() == null){
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

        for (Map.Entry<Integer, ItemStack> entry : data.getInventory().getItems().entrySet()) {
            if(entry.getKey() < 36 && entry.getKey() >= 0) {
                player.getInventory().setItem(entry.getKey(), entry.getValue());
            }
        }

        player.getInventory().setHelmet(data.getInventory().getHelmet());
        player.getInventory().setChestplate(data.getInventory().getChest());
        player.getInventory().setLeggings(data.getInventory().getLeggings());
        player.getInventory().setBoots(data.getInventory().getBoots());

        if(data.getValues() == null){
            return;
        }

        player.getInventory().setHeldItemSlot(data.getValues().getHeldSlot());
        player.updateInventory();

    }

}
