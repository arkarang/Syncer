package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

@RequiredArgsConstructor
public class SetCurrentHealth implements ApplyStrategy {

    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.values() == null){
            return;
        }

        scheduler.runTaskLater(plugin, ()->{
            if(player.getMaxHealth() <= data.values().getHealth()){
                player.setHealth(player.getMaxHealth());
            }else {
                player.setHealth(data.values().getHealth());
            }
        }, 5L);
    }

}
