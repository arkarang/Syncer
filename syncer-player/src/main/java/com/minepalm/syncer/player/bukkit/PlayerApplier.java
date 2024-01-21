package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.bukkit.strategies.ApplyStrategy;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public PlayerData inject(Player player, PlayerData data){
        PlayerData extracted = this.extract(player);

        try{
            apply(player, data);
            return data;
        }catch (Throwable e){
            MySQLLogger.report(data, e, "inject failed");
            return extracted;
        }
    }

    public void setActivate(String name, boolean b){
        activates.put(name, b);
    }

    public boolean isActivate(String name){
        return activates.containsKey(name);
    }

    public void apply(Player player, PlayerData data){
        for (String key : orders) {
            if(activates.containsKey(key)) {
                ApplyStrategy strategy = strategies.get(key);
                try {
                    logger.log(player.getName()+": try apply "+key);
                    strategy.applyPlayer(player, data);
                } catch (Throwable e) {
                    MySQLLogger.report(data, e, "apply failed at "+key+" strategy");
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
        boolean isFly = gamemode == 1 || player.isFlying();
        double healthScale = player.getHealthScale();
        PlayerDataValues values = new PlayerDataValues(health, healthScale, level, foodLevel, exp, saturation, exhaustion, heldSlot, gamemode, isFly);
        if(!Bukkit.isPrimaryThread()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(PlayerSyncer.getInst(), () -> {
                closeInventory(player);
                future.complete(null);
            });
            try {
                future.get(100L, TimeUnit.MILLISECONDS);
            }catch (Throwable ignored){
            }
        } else {
            closeInventory(player);
        }

        PlayerDataInventory inventory = PlayerDataInventory.of(player.getInventory());
        PlayerDataEnderChest enderChest = PlayerDataEnderChest.of(player.getEnderChest());
        return new PlayerData(player.getUniqueId(), values, inventory, enderChest);
    }

    private void closeInventory(Player player){
        try {
            if (player.getOpenInventory() != null) {
                InventoryView view = player.getOpenInventory();
                ItemStack item = view.getCursor();
                if (item != null && item.getType() != Material.AIR) {
                    Map<Integer, ItemStack> leftover = view.getTopInventory().addItem(item);
                    if (leftover.size() > 0) {
                        leftover.values().forEach(leftoverItem -> view.getBottomInventory().addItem(leftoverItem));
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
