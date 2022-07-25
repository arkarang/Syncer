package com.minepalm.syncer.player.bukkit.test;

import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import com.minepalm.syncer.player.mysql.MySQLPlayerLogDatabase;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class DuplicateFinder {

    private final Logger logger;
    private final MySQLDatabase database;

    @RequiredArgsConstructor
    @Data
    public static class DuplicateEntry{
        final UUID uuid;
        final long save_id;
        final long load_id;
    }

    public void init(){
        database.execute(connection -> {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `playersyncer_duplicate_log`(" +
                    "`uuid` VARCHAR(36), " +
                    "`save` BIGINT, " +
                    "`load` BIGINT) charset=utf8mb4")
                    .execute();
        });
    }

    public void save(DuplicateEntry entry){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO `playersyncer_duplicate_log` " +
                    "(`uuid`, `save`, `load`) " +
                    "VALUES(?, ?, ?)");
            ps.setString(1, entry.uuid.toString());
            ps.setLong(2, entry.save_id);
            ps.setLong(3, entry.load_id);
            ps.execute();
        });
    }

    public void execute() throws ExecutionException, InterruptedException {
        init();
        List<PlayerDataLog> list = find().get();

        //migrate("playersyncer_dupe_backup", list).get();

        Map<UUID, List<PlayerDataLog>> map = sort(list);
        logger.info("sort complete");

        ExecutorService workers = Executors.newFixedThreadPool(4);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        int size = map.size();
        int count = 0;
        for (Map.Entry<UUID, List<PlayerDataLog>> entry : map.entrySet()) {
            count++;
            logger.info("check duplicate of " + count + "/" + size);
            List<DuplicateEntry> dupeList = findDuplicate(entry.getKey(), entry.getValue());
            for (DuplicateEntry dupe : dupeList) {
                futures.add(CompletableFuture.runAsync(() -> save(dupe), workers));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        logger.info("DuplicateFinder: complete");

    }

    public void execute2() throws ExecutionException, InterruptedException {
        init();
        List<PlayerDataLog> list = find().get();

        //migrate("playersyncer_dupe_backup", list).get();

        Map<UUID, List<PlayerDataLog>> map = sort(list);
        logger.info("sort complete");

        int size = map.size();
        int count = 0;
        for (Map.Entry<UUID, List<PlayerDataLog>> entry : map.entrySet()) {
            count++;
            UUID uuid = entry.getKey();
            int passCount = 0;
            for (PlayerDataLog log : entry.getValue()) {
                if(log.task_name.equals("PASS")){
                    passCount++;
                }
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String user = uuid.toString();
            if(player != null){
                user = player.getName()+"("+uuid.toString()+")";
            }
            if(passCount > 10){
                logger.severe("[ERROR] "+user + " pass count " + passCount);
            }else if(passCount > 5){
                logger.warning("[WARN] "+user + " pass count " + passCount);

            }else if(passCount > 0) {
                logger.info("[INFO] "+user + " pass count " + passCount);

            }

        }

        for (Map.Entry<UUID, List<PlayerDataLog>> entry : map.entrySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            logger.info("joined player: "+player.getName()+"("+player.getUniqueId()+")");
        }
        logger.info("DuplicateFinder: complete. total: "+map.size());

    }


    public CompletableFuture<Void> migrate(String table, List<PlayerDataLog> list){
        MySQLPlayerLogDatabase backupDatabase = new MySQLPlayerLogDatabase(table, database);
        backupDatabase.init();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        list.forEach(log -> {
            futures.add(backupDatabase.log(log));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    }

    public CompletableFuture<List<PlayerDataLog>> find(){
        return database.executeAsync(connection -> {
            List<PlayerDataLog> list = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid`, " +
                    "`time`, `task_id`, `inventory_data`, `enderchest_data`, `data_generated_time`, " +
                    "`description` FROM `playersyncer_logs` WHERE `time` > ? ORDER BY `time`");
            ps.setLong(1, 1658637000000L);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                UUID uuid = UUID.fromString(rs.getString(1));
                long time = rs.getLong(2);
                String taskId = rs.getString(3);
                String invData = rs.getString(4);
                String enderchestData = rs.getString(5);
                long dataTime = rs.getLong(6);
                String desc = rs.getString(7);
                PlayerDataLog log = new PlayerDataLog(taskId, uuid, invData, enderchestData, time, dataTime);
                list.add(log);
            }
            logger.info("DuplicateFinder: find "+list.size()+" logs");
            return list;
        });
    }

    public Map<UUID, List<PlayerDataLog>> sort(List<PlayerDataLog> list){
        Map<UUID, List<PlayerDataLog>> map = new ConcurrentHashMap<>();
        for (PlayerDataLog log : list) {
            if(!map.containsKey(log.uuid)){
                map.put(log.uuid, new ArrayList<>());
            }
            map.get(log.uuid).add(log);
        }
        logger.info("DuplicateFinder: sort "+map.size()+" uuids");
        return map;
    }

    public List<DuplicateEntry> findDuplicate(UUID uuid, List<PlayerDataLog> list){
        PlayerDataLog latestSave = null, lastLoad = null;
        List<DuplicateEntry> results = new ArrayList<>();
        Map<Long, Long> dupeMap = new HashMap<>();
        OfflinePlayer player = Bukkit.getPlayer(uuid);
        String user = uuid.toString();
        if(player != null){
            user = player.getName()+"("+uuid.toString()+")";
        }
        long taskTime = 0;
        for (PlayerDataLog log : list) {
            if(taskTime > log.log_generated_time){
                logger.severe("wrong sort");
                throw new IllegalArgumentException("wrong sort");
            }
            taskTime = log.log_generated_time;
            if(isLoadTask(log)){
                lastLoad = log;
                this.logger.info(user+"load last: "+lastLoad.data_generated_time);
                if (latestSave != null) {
                    if(lastLoad.data_generated_time < latestSave.data_generated_time){

                        if(!dupeMap.containsKey(lastLoad.data_generated_time)){
                            DuplicateEntry entry = new DuplicateEntry(uuid, latestSave.data_generated_time, lastLoad.data_generated_time);
                            logger.warning("DuplicateFinder: find duplication "+user+" at "+entry);
                            results.add(entry);
                            dupeMap.put(lastLoad.data_generated_time, latestSave.data_generated_time    );
                        }


                    }
                }
            }else if(isSaveTask(log)){
                if(latestSave != null){
                    if(latestSave.data_generated_time < log.data_generated_time){
                        latestSave = log;
                    }
                }else{
                    latestSave = log;
                }
                this.logger.info(user+"save latest: "+latestSave.data_generated_time);
            }
        }
        if(!dupeMap.isEmpty())
            this.logger.warning("found duplicate "+user+" of "+dupeMap.size()+" times ");
        return results;
    }

    private boolean isLoadTask(PlayerDataLog log){
        return log.task_name.equals("LOAD");
    }

    private boolean isSaveTask(PlayerDataLog log){
        return log.task_name.equals("SAVE") || log.task_name.equals("AUTO_SAVE");
    }
}
