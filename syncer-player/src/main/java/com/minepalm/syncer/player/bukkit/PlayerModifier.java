package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerModifier {

    ConcurrentHashMap<String, LoadStrategy> strategies = new ConcurrentHashMap<>();

    public void addStrategy(String name, LoadStrategy strategy){
        strategies.put(name, strategy);
    }

    void inject(Player player, PlayerData data){
        PlayerData extracted = this.extract(player);

        try{
            apply(player, data);
        }catch (Throwable e){
            e.printStackTrace();
            apply(player, extracted);
        }
    }

    void apply(Player player, PlayerData data){
        for (LoadStrategy strategy : strategies.values()) {
            strategy.applyPlayer(player, data);
        }
    }

    PlayerData extract(Player player){
        double health = player.getHealth();
        int level = player.getLevel();
        int foodLevel = player.getFoodLevel();
        float exp = player.getExp();
        float saturation = player.getSaturation();
        float exhaustion = player.getExhaustion();
        int heldSlot = player.getInventory().getHeldItemSlot();
        PlayerDataValues values = new PlayerDataValues(health, level, foodLevel, exp, saturation, exhaustion, heldSlot);
        PlayerDataInventory inventory = PlayerDataInventory.of(player.getInventory());
        PlayerDataEnderChest enderChest = PlayerDataEnderChest.of(player.getEnderChest());
        return new PlayerData(player.getUniqueId(), values, inventory, enderChest);
    }
}
