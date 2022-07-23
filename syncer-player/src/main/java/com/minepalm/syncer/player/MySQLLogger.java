package com.minepalm.syncer.player;

import com.minepalm.syncer.player.bukkit.PlayerDataLog;
import com.minepalm.syncer.player.mysql.MySQLExceptionLogDatabase;
import com.minepalm.syncer.player.mysql.MySQLPlayerLogDatabase;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MySQLLogger {

    private static MySQLExceptionLogDatabase exceptionLogDatabase;
    private static MySQLPlayerLogDatabase playerLogDatabase;
    private static final Lock lock = new ReentrantLock();

    public static void init(MySQLDatabase mysql){
        synchronized (lock){
            if(exceptionLogDatabase == null){
                exceptionLogDatabase = new MySQLExceptionLogDatabase("playersyncer_excepitons", mysql);
                exceptionLogDatabase.init();
            }
            if(playerLogDatabase == null){
                playerLogDatabase = new MySQLPlayerLogDatabase("playersyncer_logs", mysql);
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
        ex.printStackTrace();
        if(exceptionLogDatabase != null){
            exceptionLogDatabase.log(ex, System.currentTimeMillis());
        }
    }

    public static void purge(long baseTime){
        if(playerLogDatabase != null){
            playerLogDatabase.purge(baseTime);
        }
    }
}
