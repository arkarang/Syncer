package com.minepalm.syncer.player.mysql;

import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
                    "`inventory_data` MEDIUMBLOB, " +
                    "`enderchest_data` MEDIUMBLOB, " +
                    "`data_generated_time` BIGINT DEFAULT 0, "+
                    "`description` TEXT, "+
                    "PRIMARY KEY(`row_id`)) " +
                    "charset=utf8mb4");
            //
            ps.execute();
        });
    }

    public CompletableFuture<Void> log(PlayerDataLog log, String description){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" " +
                    "(`uuid`, `time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, `description`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?) ");
            ps.setString(1, log.uuid.toString());
            ps.setLong(2, log.log_generated_time);
            ps.setString(3, log.task_name);
            ps.setBytes(4, log.inventoryData);
            ps.setBytes(5, log.enderChestData);
            ps.setLong(6, log.data_generated_time);
            ps.setString(7, description);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> log(PlayerDataLog log){
        return log(log, "");
    }

    public CompletableFuture<List<Pair<PlayerDataLog, String>>> select(UUID uuid, long range){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, " +
                    "`description` FROM "+table+" WHERE `uuid`=? AND `time` > ? ORDER BY `time`");
            ps.setString(1, uuid.toString());
            ps.setLong(2, range);
            ResultSet rs = ps.executeQuery();
            List<Pair<PlayerDataLog, String>> list = new ArrayList<>();
            while (rs.next()){
                long time = rs.getLong(1);
                String taskId = rs.getString(2);
                byte[] invData = rs.getBytes(3);
                byte[] enderchestData = rs.getBytes(4);
                long dataTime = rs.getLong(5);
                String desc = rs.getString(6);
                PlayerDataLog log = new PlayerDataLog(taskId, uuid, invData, enderchestData, time, dataTime);
                list.add(new Pair<>(log, desc));
            }
            return list;
        });
    }

    public CompletableFuture<List<Pair<PlayerDataLog, String>>> select(UUID uuid, long range, long rangeMax){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, " +
                    "`description` FROM "+table+" WHERE `uuid`=? AND `time` > ? AND `time` <= ? ORDER BY `time`");
            ps.setString(1, uuid.toString());
            ps.setLong(2, range);
            ps.setLong(3, rangeMax);
            ResultSet rs = ps.executeQuery();
            List<Pair<PlayerDataLog, String>> list = new ArrayList<>();
            while (rs.next()){
                long time = rs.getLong(1);
                String taskId = rs.getString(2);
                byte[] invData = rs.getBytes(3);
                byte[] enderchestData = rs.getBytes(4);
                long dataTime = rs.getLong(5);
                String desc = rs.getString(6);
                PlayerDataLog log = new PlayerDataLog(taskId, uuid, invData, enderchestData, time, dataTime);
                list.add(new Pair<>(log, desc));
            }
            return list;
        });
    }

    public CompletableFuture<List<Pair<PlayerDataLog, String>>> selectType(UUID uuid, String type, long range, long rangeMax){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("" +
                    "SELECT `time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, " +
                    "`description` FROM "+table+" WHERE `uuid`=? AND `time` > ? AND `time` <= ? AND `type`=? ORDER BY `time`");
            ps.setString(1, uuid.toString());
            ps.setLong(2, range);
            ps.setLong(3, rangeMax);
            ps.setString(4, type);
            ResultSet rs = ps.executeQuery();
            List<Pair<PlayerDataLog, String>> list = new ArrayList<>();
            while (rs.next()){
                long time = rs.getLong(1);
                String taskId = rs.getString(2);
                byte[] invData = rs.getBytes(3);
                byte[] enderchestData = rs.getBytes(4);
                long dataTime = rs.getLong(5);
                String desc = rs.getString(6);
                PlayerDataLog log = new PlayerDataLog(taskId, uuid, invData, enderchestData, time, dataTime);
                list.add(new Pair<>(log, desc));
            }
            return list;
        });
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
