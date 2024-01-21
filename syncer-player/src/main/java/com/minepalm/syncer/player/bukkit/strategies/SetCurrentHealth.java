package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

@RequiredArgsConstructor
public class SetCurrentHealth implements ApplyStrategy {

    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getValues() == null){
            return;
        }

        scheduler.runTaskLater(plugin, ()->{
            try {
                player.setHealth(data.getValues().getHealth());
            } catch (Throwable e) {
                player.setHealth(player.getMaxHealth());
            }
        }, 15L);
    }


    private static class Rendog {
        //if line 2 of lore of player's chestplate contain "&c&l[ &f&l체력 &c&l]":
        //		set {_lore::chestplate} to line 2 of lore of player's chestplate
        //		replace all "&c&l[ &f&l체력 &c&l] &7&l::" and "체력" and "&f" and "&l" and " " with "" in {_lore::chestplate}
        //		add {_lore::chestplate} parsed as integer to {_health::%player%}
        //
        //	if line 2 of lore of player's leggings contain "&c&l[ &f&l체력 &c&l]":
        //		set {_lore::leggings} to line 2 of lore of player's leggings
        //		replace all "&c&l[ &f&l체력 &c&l] &7&l::" and "체력" and "&f" and "&l" and " " with "" in {_lore::leggings}
        //		add {_lore::leggings} parsed as integer to {_health::%player%}
        //
        //	if line 2 of lore of player's boots contain "&c&l[ &f&l체력 &c&l]":
        //		set {_lore::boots} to line 2 of lore of player's boots
        //		replace all "&c&l[ &f&l체력 &c&l] &7&l::" and "체력" and "&f" and "&l" and " " with "" in {_lore::boots}
        //		add {_lore::boots} parsed as integer to {_health::%player%}
        //
        //	set player's max health to 20+{_health::%player%}
        //	if player's health > player's max health:
        //		set player's health to player's max health

        public static int calculate(ItemStack item) {
            if (item == null) return 0;
            if (item.getItemMeta() == null) return 0;
            if (item.getItemMeta().getLore() == null) return 0;
            if (item.getItemMeta().getLore().size() < 2) return 0;
            if (!item.getItemMeta().getLore().get(1).contains("&c&l[ &f&l체력 &c&l]")) return 0;
            String lore = item.getItemMeta().getLore().get(1);
            lore = lore.replace("&c&l[ &f&l체력 &c&l] &7&l::", "");
            lore = lore.replace("체력", "");
            lore = lore.replace("&f", "");
            lore = lore.replace("&l", "");
            lore = lore.replace(" ", "");
            return Integer.parseInt(lore);
        }
    }
}
