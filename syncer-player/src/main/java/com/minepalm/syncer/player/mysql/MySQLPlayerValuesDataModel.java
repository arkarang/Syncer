package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.bukkit.PlayerDataValues;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLPlayerValuesDataModel {

    private final String table;
    private final MySQLDatabase database;

    void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`uuid` VARCHAR(36), " +
                    "`health` DOUBLE DEFAULT 20, " +
                    "`level` INT DEFAULT 0, " +
                    "`foodLevel` INT DEFAULT 20, "+
                    "`exp` FLOAT DEFAULT 0, "+
                    "`saturation` FLOAT DEFAULT 0, "+
                    "`exhaustion` FLOAT DEFAULT 0, "+
                    "`heldSlot` INT DEFAULT 0, " +
                    "PRIMARY KEY(`uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<PlayerDataValues> load(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `health`, `level`, `foodLevel`, `exp`, `saturation`, `exhaustion`, `heldSlot` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                double health = rs.getDouble(1);
                int level = rs.getInt(2);
                int foodLevel = rs.getInt(3);
                int exp = rs.getInt(4);
                float saturation = rs.getFloat(5);
                float exhaustion = rs.getFloat(6);
                int heldSlot = rs.getInt(7);
                return new PlayerDataValues(health, level, foodLevel, exp, saturation, exhaustion, heldSlot);
            }else{
                return PlayerDataValues.getDefault();
            }
        });
    }

    CompletableFuture<Void> save(UUID uuid, PlayerDataValues values){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" " +
                    "(`uuid`, `health`, `level`, `foodlLevel`, `exp`, `saturation`, `exhaustion`, `heldSlot`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "`health`=VALUES(`health`), " +
                    "`level`=VALUES(`level`), " +
                    "`foodLevel`=VALUES(`foodLevel`), " +
                    "`exp`=VALUES(`exp`), " +
                    "`saturation`=VALUES(`saturation`), " +
                    "`exhaustion`=VALUES(`exhaustion`)" +
                    "`heldSlot`=VALUES(`heldSlot`);");
            ps.setString(1, uuid.toString());
            ps.setDouble(2, values.getHealth());
            ps.setInt(3, values.getLevel());
            ps.setInt(4, values.getFoodLevel());
            ps.setFloat(5, values.getExp());
            ps.setFloat(6, values.getSaturation());
            ps.setFloat(7, values.getExhaustion());
            ps.setInt(8, values.getHeldSlot());
            ps.execute();
            return null;
        });
    }
}
