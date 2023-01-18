package com.minepalm.syncer.player;

import com.minepalm.syncer.player.bukkit.PlayerSyncer;
import com.minepalm.syncer.player.data.PlayerDataLog;
import com.minepalm.syncer.player.mysql.MySQLExceptionLogDatabase;
import com.minepalm.syncer.player.mysql.MySQLPlayerLogDatabase;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MySQLLogger {

    private static MySQLExceptionLogDatabase exceptionLogDatabase;
    private static MySQLPlayerLogDatabase playerLogDatabase;
    private static final Lock lock = new ReentrantLock();

    public static void init(MySQLPlayerLogDatabase logdb, MySQLExceptionLogDatabase exdb){
        synchronized (lock){
            if(exceptionLogDatabase == null){
                exceptionLogDatabase = exdb;
                exceptionLogDatabase.init();
            }
            if(playerLogDatabase == null){
                playerLogDatabase = logdb;
                playerLogDatabase.init();
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
            return playerLogDatabase.log(log, description);
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    public static void log(Throwable ex){
        if(exceptionLogDatabase != null){
            exceptionLogDatabase.log(ex, System.currentTimeMillis());
            Bukkit.getScheduler().runTask(PlayerSyncer.getInst(), ()->{ex.printStackTrace();});
        }
    }

    public static void purge(long baseTime){
        if(playerLogDatabase != null){
            playerLogDatabase.purge(baseTime);
        }
    }
}
