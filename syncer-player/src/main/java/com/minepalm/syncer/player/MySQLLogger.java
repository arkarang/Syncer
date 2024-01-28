package com.minepalm.syncer.player;

import com.minepalm.syncer.bootstrap.SyncerBukkit;
import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import com.minepalm.syncer.player.mysql.MySQLErrorReportDatabase;
import com.minepalm.syncer.player.mysql.MySQLPlayerLogDatabase;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MySQLLogger {

    private static MySQLPlayerLogDatabase playerLogDatabase;
    private static MySQLErrorReportDatabase reportdb;
    private static String current;
    private static final Lock lock = new ReentrantLock();

    public static void init(String server,
                            MySQLPlayerLogDatabase logdb,
                            MySQLErrorReportDatabase reportz){
        synchronized (lock){
            current = server;
            if(playerLogDatabase == null){
                playerLogDatabase = logdb;
                playerLogDatabase.init();
            }
            if(reportdb == null){
                reportdb = reportz;
                reportdb.init();
            }
        }
    }

    public static CompletableFuture<Void> log(PlayerDataLog log){
        if(playerLogDatabase != null){
            return playerLogDatabase.log(log);
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    public static CompletableFuture<Void> log(PlayerDataLog log, String description){
        if(playerLogDatabase != null){
            return playerLogDatabase.log(log, "["+current+"] "+description);
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    public static void report(PlayerData data, Throwable ex, String description){
        Bukkit.getLogger().warning("Player data error of "+data.getUuid()+" : " + description);
        if(reportdb != null) {
            String currentServer = SyncerBukkit.inst().getHolderRegistry().getLocalName();
            long now = System.currentTimeMillis();
            Date date = new Date();
            ErrorReport report = new ErrorReport(data.getUuid(), currentServer, data, description, ex, now);
            reportdb.log(report).join();
            playerLogDatabase.log(PlayerDataLog.dump("DUMP", data), report.uuid + "_" + currentServer + "_" + date);
        }
    }

    public static void report(UUID uuid, Throwable ex, String description){
        Bukkit.getLogger().warning("Player data error of "+uuid+" : " + description);
        if(reportdb != null) {
            String currentServer = SyncerBukkit.inst().getHolderRegistry().getLocalName();
            long now = System.currentTimeMillis();
            ErrorReport report = new ErrorReport(uuid, currentServer, null, description, ex, now);
            reportdb.log(report).join();
        }
    }

    public static void purge(long baseTime){
        if(playerLogDatabase != null){
            playerLogDatabase.purge(baseTime);
        }
    }
}
