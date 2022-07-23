package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;

// row_id / uuid / executed_time / task_id / data_id(generated_time) / description / data

@RequiredArgsConstructor
public class MySQLPlayerLogDatabase {

    private final String table;
    private final MySQLDatabase database;

    public void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`uuid` VARCHAR(36), " +
                    "`time` BIGINT, "+
                    "`task_id` VARCHAR(16), "+
                    "`inventory_data` TEXT, " +
                    "`enderchest_data` TEXT, " +
                    "`data_generated_time` BIGINT DEFAULT 0, "+
                    "`description` TEXT, "+
                    "PRIMARY KEY(`row_id`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> log(PlayerDataLog log, String description){
        return database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" " +
                    "(`uuid`, `time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, `description`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?) ");
            ps.setString(1, log.uuid.toString());
            ps.setLong(2, log.log_generated_time);
            ps.setString(3, log.task_name);
            ps.setString(4, log.inventoryData);
            ps.setString(5, log.enderChestData);
            ps.setLong(6, log.data_generated_time);
            ps.setString(7, description);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> log(PlayerDataLog log){
        return log(log, "");
    }

    public CompletableFuture<Void> purge(long baseTime){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `time` < ? ");
            ps.setLong(1, baseTime);
            ps.execute();
            return null;
        });
    }
}
