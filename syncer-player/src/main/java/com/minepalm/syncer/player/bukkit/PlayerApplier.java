package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerApplier {

    ConcurrentHashMap<String, LoadStrategy> strategies = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Boolean> activates = new ConcurrentHashMap<>();

    public void registerStrategy(String name, LoadStrategy strategy){
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

    public void setActivate(String name, boolean b){
        activates.put(name, b);
    }

    void apply(Player player, PlayerData data){
        List<String> keys = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : activates.entrySet()) {
            if(entry.getValue()){
                keys.add(entry.getKey());
            }
        }

        for (String key : keys) {
            LoadStrategy strategy = strategies.get(key);
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
