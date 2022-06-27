package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerDataEnderChest;
import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import com.minepalm.syncer.player.bukkit.PlayerDataValues;
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

    public CompletableFuture<PlayerData> load(){
        val inventory = inventoryDataModel.load(uuid);
        val values = valuesDataModel.load(uuid);
        val enderChest = enderChestDataModel.load(uuid);

        return CompletableFuture.allOf(inventory, values, enderChest).thenApply(ignored -> {
            try {
                PlayerDataInventory inv = inventory.get();
                PlayerDataValues data = values.get();
                PlayerDataEnderChest enderChestData = enderChest.get();
                return new PlayerData(uuid, data, inv, enderChestData);
            }catch (InterruptedException | ExecutionException e){
                return new PlayerData(uuid, null, null, null);
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data){
        val inventoryFuture = inventoryDataModel.save(uuid, data.getInventory());
        val valuesFuture = valuesDataModel.save(uuid, data.getValues());
        val enderChestFuture = enderChestDataModel.save(uuid, data.getEnderChest());

        inventoryFuture.thenAccept(triedDuplicated -> {
            if(triedDuplicated){
                Bukkit.getLogger().warning("found tried duplicate saving "+data.getUuid());
            }
        });

        return CompletableFuture.allOf(inventoryFuture, valuesFuture, enderChestFuture);
    }

    public CompletableFuture<Void> save(PlayerData data, long afterMills){
        return CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(afterMills);
            }catch (Throwable e){

            }
        }).thenCompose(ignored -> {
            val inventoryFuture = inventoryDataModel.save(uuid, data.getInventory());
            val valuesFuture = valuesDataModel.save(uuid, data.getValues());
            val enderChestFuture = enderChestDataModel.save(uuid, data.getEnderChest());
            return CompletableFuture.allOf(inventoryFuture, valuesFuture, enderChestFuture);
        });
    }


}
