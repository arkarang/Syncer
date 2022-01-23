package com.minepalm.syncer.player.mysql;

import com.minepalm.arkarangutils.compress.CompressedInventorySerializer;
import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLPlayerInventoryDataModel {


    private final String table;
    private final MySQLDatabase database;

    public void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`uuid` VARCHAR(36), " +
                    "`data` TEXT, " +
                    "PRIMARY KEY(`uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<PlayerDataInventory> load(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `data` FROM "+table+" WHERE `uuid`=? FOR UPDATE");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                try {
                    HashMap<Integer, ItemStack> map = new HashMap<>();
                    ItemStack[] items = CompressedInventorySerializer.itemStackArrayFromBase64(rs.getString(1));
                    for(int i = 0 ; i < 36 ; i++){
                        map.put(i, items[i]);
                    }

                    return PlayerDataInventory.of(map);
                }catch (Throwable ignored){

                }
            }

            return PlayerDataInventory.empty();

        });
    }

    CompletableFuture<Void> save(UUID uuid, PlayerDataInventory inventory){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `data`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `data`=VALUES(`data`)");
            ps.setString(1, uuid.toString());
            ps.setString(2, CompressedInventorySerializer.itemStackArrayToBase64(inventory.toArray()));
            ps.execute();
            return null;
        });
    }

}
