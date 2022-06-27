package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.bukkit.strategies.ApplyStrategy;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PlayerApplier {

    private final TimestampLogger logger;
    private final List<String> orders = new ArrayList<>();
    private final ConcurrentHashMap<String, ApplyStrategy> strategies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> activates = new ConcurrentHashMap<>();

    public void registerStrategy(String name, ApplyStrategy strategy){
        strategies.put(name, strategy);
        orders.add(name);
    }

    public void inject(Player player, PlayerData data){
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

    public void apply(Player player, PlayerData data){
        for (String key : orders) {
            if(activates.containsKey(key)) {
                ApplyStrategy strategy = strategies.get(key);
                try {
                    logger.log(player.getName()+": try apply "+key);
                    strategy.applyPlayer(player, data);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public PlayerData extract(Player player){
        double health = player.getHealth();
        int level = player.getLevel();
        int foodLevel = player.getFoodLevel();
        float exp = player.getExp();
        float saturation = player.getSaturation();
        float exhaustion = player.getExhaustion();
        int heldSlot = player.getInventory().getHeldItemSlot();
        int gamemode = player.getGameMode().getValue();
        boolean isFly = player.isFlying();
        double healthScale = player.getHealthScale();
        PlayerDataValues values = new PlayerDataValues(health, healthScale, level, foodLevel, exp, saturation, exhaustion, heldSlot, gamemode, isFly);
        PlayerDataInventory inventory = PlayerDataInventory.of(player.getInventory());
        PlayerDataEnderChest enderChest = PlayerDataEnderChest.of(player.getEnderChest());
        return new PlayerData(player.getUniqueId(), values, inventory, enderChest);
    }
}
