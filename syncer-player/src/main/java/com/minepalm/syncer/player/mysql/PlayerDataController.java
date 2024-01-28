package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.bukkit.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class PlayerDataController {

    private final UUID uuid;
    private final MySQLPlayerInventoryDataModel inventoryDataModel;
    private final MySQLPlayerValuesDataModel valuesDataModel;
    private final MySQLPlayerEnderChestDataModel enderChestDataModel;

    private final MySQLPlayerPotionDatabase potionDatabase;

    public CompletableFuture<PlayerData> load(){
        val inventory = inventoryDataModel.load(uuid);
        val values = valuesDataModel.load(uuid);
        val enderChest = enderChestDataModel.load(uuid);
        val potion = potionDatabase.load(uuid);

        return CompletableFuture.allOf(inventory, values, enderChest, potion).thenApplyAsync(ignored -> {
            try {
                PlayerDataInventory inv = inventory.get();
                PlayerDataValues data = values.get();
                PlayerDataEnderChest enderChestData = enderChest.get();
                PlayerDataPotion potionData = potion.get();

                return new PlayerData(uuid, data, inv, enderChestData, potionData);
            }catch (Throwable e){
                MySQLLogger.report(uuid, e, "Failed to load player data");
                return null;
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data){
        val inventoryFuture = inventoryDataModel.save(uuid, data.getInventory());
        val valuesFuture = valuesDataModel.save(uuid, data.getValues());
        val enderChestFuture = enderChestDataModel.save(uuid, data.getEnderChest());
        val potionFuture = potionDatabase.save(uuid, data.getPotionEffects());

        return CompletableFuture.allOf(inventoryFuture, valuesFuture, enderChestFuture, potionFuture);
    }

}
