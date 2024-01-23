package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.mysql.*;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class PlayerDataStorage {

    private final MySQLPlayerEnderChestDataModel enderChestDataModel;
    private final MySQLPlayerValuesDataModel valuesDataModel;
    private final MySQLPlayerInventoryDataModel inventoryDataModel;
    private final MySQLPlayerPotionDatabase potionDatabase;

    public PlayerDataStorage(MySQLDatabase database) {
        enderChestDataModel
                = new MySQLPlayerEnderChestDataModel("playersyncer_enderchest", database);
        inventoryDataModel
                = new MySQLPlayerInventoryDataModel("playersyncer_inventory", database);
        valuesDataModel
                = new MySQLPlayerValuesDataModel("playersyner_values", database);
        potionDatabase
                = new MySQLPlayerPotionDatabase("playersyncer_potion", database);

        enderChestDataModel.init();
        inventoryDataModel.init();
        valuesDataModel.init();
        potionDatabase.init();
    }

    private PlayerDataController getController(UUID uuid){
        return new PlayerDataController(uuid, inventoryDataModel, valuesDataModel, enderChestDataModel, potionDatabase);
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return getController(uuid).load();
    }

    public CompletableFuture<Void> save(UUID uuid, PlayerData data){
        return getController(uuid).save(data);
    }

}
