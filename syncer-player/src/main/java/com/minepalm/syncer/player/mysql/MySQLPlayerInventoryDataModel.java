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
                    "`generated_time` BIGINT DEFAULT 0, "+
                    "PRIMARY KEY(`uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public void alter(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("ALTER TABLE "+table+" ADD COLUMN `generated_time` BIGINT DEFAULT 0");
            ps.execute();
        });
    }

    public CompletableFuture<PlayerDataInventory> load(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `data`, `generated_time` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                HashMap<Integer, ItemStack> map = new HashMap<>();
                ItemStack[] items = CompressedInventorySerializer.itemStackArrayFromBase64(rs.getString(1));
                for(int i = 0 ; i < items.length ; i++){
                    map.put(i, items[i]);
                }

                return PlayerDataInventory.of(map, rs.getLong(2));
            }

            return null;

        });
    }

    /*
    public CompletableFuture<Boolean> save(UUID uuid, PlayerDataInventory inventory){
        return database.executeAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                PreparedStatement select = connection.prepareStatement("SELECT `generated_time` FROM " + table + " WHERE `uuid`=? FOR UPDATE");
                select.setString(1, uuid.toString());
                ResultSet rs = select.executeQuery();
                long generatedTime = 0;
                if(rs.next()){
                    generatedTime = rs.getLong(1);
                }
                if(generatedTime < inventory.getGeneratedTime()){
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table + " (`uuid`, `data`, `generated_time`) VALUES(?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE `data`=VALUES(`data`), `generated_time`=VALUES(`generated_time`)");
                    ps.setString(1, uuid.toString());
                    ps.setString(2, CompressedInventorySerializer.itemStackArrayToBase64(inventory.toArray()));
                    ps.setLong(3, inventory.getGeneratedTime());
                    ps.execute();
                }else{
                    return false;
                }
                connection.commit();
            }finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

     */


    public CompletableFuture<Boolean> save(UUID uuid, PlayerDataInventory inventory){
        return database.executeAsync(connection -> {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table + " (`uuid`, `data`, `generated_time`) VALUES(?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE `data`=VALUES(`data`), `generated_time`=VALUES(`generated_time`)");
            ps.setString(1, uuid.toString());
            ps.setString(2, CompressedInventorySerializer.itemStackArrayToBase64(inventory.toArray()));
            ps.setLong(3, inventory.getGeneratedTime());
            ps.execute();

            return true;
        });
    }
}
