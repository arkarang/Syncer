package com.minepalm.syncer.player.mysql;

import com.minepalm.syncer.player.ErrorReport;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MySQLErrorReportDatabase {

    //
    //    public final UUID uuid;
    //    public final String server;
    //
    //    public final PlayerData playerData;
    //
    //    public final String description;
    //    public final Throwable exception;
    //    public final long time;

    private final String table;
    private final MySQLDatabase database;

    public MySQLErrorReportDatabase(String table, MySQLDatabase database) {
        this.table = table;
        this.database = database;
        init();
    }

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`uuid` VARCHAR(36), " +
                    "`server` VARCHAR(16)," +
                    "`time` BIGINT, " +
                    "`exception_name` VARCHAR(32), " +
                    "`description` TEXT, " +
                    "`data` TEXT, " +
                    "PRIMARY KEY(`row_id`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> log(ErrorReport report) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table + " " +
                    "(`uuid`, `server`, `time`, `exception_name`, `description`, `data`) " +
                    "VALUES(?, ?, ?, ?, ?, ?) ");
            ps.setString(1, report.uuid.toString());
            ps.setString(2, report.server);
            ps.setLong(3, report.time);
            ps.setString(4, report.exception.getClass().getSimpleName());
            ps.setString(5, report.description);
            if (report.exception.getCause() == null) {
                ps.setString(6, Arrays.toString(report.exception.getStackTrace()));
            } else {
                ps.setString(6, Arrays.toString(report.exception.getCause().getStackTrace()));
            }
            ps.execute();
            return null;
        });
    }
}
