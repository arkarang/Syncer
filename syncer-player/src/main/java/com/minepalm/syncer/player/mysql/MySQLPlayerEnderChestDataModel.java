package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.bukkit.PlayerDataEnderChest;
import com.minepalm.syncer.player.bukkit.serialize.InvSerializer;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLPlayerEnderChestDataModel {

    private final String table;
    private final MySQLDatabase database;

    public void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`uuid` VARCHAR(36), " +
                    "`data` MEDIUMBLOB, " +
                    "PRIMARY KEY(`uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<PlayerDataEnderChest> load(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `data` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                try {
                    HashMap<Integer, ItemStack> map = new HashMap<>();
                    ItemStack[] items = InvSerializer.deserialize(rs.getBytes(1));
                    for(int i = 0 ; i < items.length ; i++){
                        map.put(i, items[i]);
                    }

                    return PlayerDataEnderChest.of(map);
                }catch (Throwable ignored){

                }
            }

            return null;

        });
    }

    public CompletableFuture<Void> save(UUID uuid, PlayerDataEnderChest enderChest){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `data`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `data`=VALUES(`data`)");
            ps.setString(1, uuid.toString());
            ps.setBytes(2, InvSerializer.serialize(enderChest.toArray()));
            ps.execute();
            return null;
        });
    }

}
