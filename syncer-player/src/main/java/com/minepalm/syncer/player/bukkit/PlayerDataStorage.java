package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.mysql.MySQLPlayerEnderChestDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerValuesDataModel;
import com.minepalm.syncer.player.mysql.PlayerDataController;
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

    private PlayerDataController getController(UUID uuid){
        return new PlayerDataController(uuid, inventoryDataModel, valuesDataModel, enderChestDataModel);
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return getController(uuid).load();
    }

    public CompletableFuture<Void> save(UUID uuid, PlayerData data){
        return getController(uuid).save(data);
    }

}
