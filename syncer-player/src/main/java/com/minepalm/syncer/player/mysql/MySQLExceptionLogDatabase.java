package com.minepalm.syncer.player.mysql;

import com.minepalm.library.database.JavaDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLExceptionLogDatabase {
    private final String table;
    private final JavaDatabase<Connection> database;

    public void init(){
        database.run(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`time` BIGINT, "+
                    "`exception_name` VARCHAR(32), "+
                    "`description` TEXT, "+
                    "`data` TEXT, "+
                    "PRIMARY KEY(`row_id`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> log(Throwable ex, long time){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`time`, `exception_name`, `description`, `data`) " +
                    "VALUES(?, ?, ?, ?) ");
            ps.setLong(1, time);
            ps.setString(2, ex.getClass().getSimpleName());
            ps.setString(3, ex.getMessage());
            if(ex.getCause() != null){
                ps.setString(4, Arrays.toString(ex.getCause().getStackTrace()));
            }else {
                ps.setString(4, Arrays.toString(ex.getStackTrace()));
            }
            ps.execute();
            return null;
        });
    }
}
