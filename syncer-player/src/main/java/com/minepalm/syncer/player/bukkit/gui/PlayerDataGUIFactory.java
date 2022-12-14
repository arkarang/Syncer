package com.minepalm.syncer.player.bukkit.gui;

import com.minepalm.library.bukkit.inventory.Inv;
import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDataGUIFactory {

    private final MySQLPlayerInventoryDataModel database;

    public PlayerDataGUI build(UUID uuid, PlayerDataInventory inventory){
        return new PlayerDataGUI(uuid, inventory.toArray());
    }

    public PlayerDataGUI build(UUID uuid, PlayerDataLog inventory) throws IOException {
        ItemStack[] items = Inv.getV1_19Compress().itemStackArrayFromBase64(inventory.getInventoryData());
        return new PlayerDataGUI(uuid, items);
    }

    public PlayerDataModifyGUI modifyGUI(UUID uuid, PlayerDataInventory inventory){
        return new PlayerDataModifyGUI(uuid, inventory.toArray(), database );
    }
    public PlayerDataModifyGUI modifyGUI(UUID uuid, PlayerDataLog inventory) throws IOException {
        ItemStack[] items = Inv.getV1_19Compress().itemStackArrayFromBase64(inventory.getInventoryData());
        return new PlayerDataModifyGUI(uuid, items, database);
    }
}
