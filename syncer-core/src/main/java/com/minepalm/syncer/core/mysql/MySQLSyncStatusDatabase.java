package com.minepalm.syncer.core.mysql;

import com.minepalm.syncer.api.SyncStage;
import com.minepalm.syncer.core.HoldData;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLSyncStatusDatabase {

    // key (String) / holder proxy / holder server / stage / timeout

    private final String table;
    private final MySQLDatabase database;

    void init(){
        this.database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " ( " +
                    "`row_id` BIGINT AUTO_INCREMENT UNIQUE, " +
                    "`objectId` VARCHAR(64), " +
                    "`proxy` VARCHAR(32), " +
                    "`server` VARCHAR(32), " +
                    "`stage` TINYINT, " +
                    "`timeout` BIGINT DEFAULT 0, " +
                    "PRIMARY KEY(`objectId`)) " +
                    "charset=utf8mb4");
            ps.execute();

            PreparedStatement createProcedure = connection.prepareStatement("" +
                    "DELIMITER $$ " +
                    "DROP PROCEDURE IF EXISTS `updateHold` $$ " +
                    "CREATE PROCEDURE `updateHold` " +
                    "(" +
                    "m_objectId VARCHAR(64), " +
                    "m_serverIn VARCHAR(32), " +
                    "m_proxyIn VARCHAR(32), " +
                    "m_currentTime BIGINT, " +
                    "m_stage VARCHAR(16), "+
                    "m_timeoutDuration BIGINT " +
                    ")" +
                    "BEGIN " +
                    "DECLARE `m_serverName` VARCHAR(32); " +
                    "DECLARE `m_proxyName` VARCHAR(32); "+
                    "DECLARE `m_timeout` BIGINT; "+
                    "DECLARE `m_isHeld` BOOLEAN; "+
                    "DECLARE `m_isTimeout` BOOLEAN; " +
                    "DECLARE `result` BOOLEAN; "+
                    " "+
                    "START TRANSACTION; "+
                    " "+
                    "SELECT `timeout` INTO `m_timeout` FROM " + table + " WHERE `objectId`=m_objectId; FOR UPDATE "+
                    "SELECT `server` INTO `m_serverName` FROM "+ table + " WHERE `objectId`=m_objectId; FOR UPDATE "+
                    "SELECT `proxy` INTO `m_proxyName` FROM "+ table + " WHERE `objectId`=m_objectId; FOR UPDATE "+
                    " "+
                    "SET `m_serverName` = IF(`m_serverName` = NULL, `m_serverIn`, `m_serverName`); "+
                    "SET `m_proxyName` = IF(`m_serverName` = NULL, `m_proxyIn`, `m_proxyName`); "+
                    " "+
                    "SET `m_isHeld` IF(`m_serverIn` = `m_serverName` AND `m_proxyName` = `m_proxyIn`, true, false); " +
                    "SET `m_isTimeout` IF(`m_currentTime` >= `m_timeout`, true, false); " +

                    "IF `m_isHeld` OR `m_isTimeout` " +
                    "THEN "+
                    "   INSERT INTO "+table+" " +
                    "       (`objectId`, `proxy`, `server`, `stage`, `timeout`) " +
                    "       VALUES(`m_objectId`, `m_proxyIn`, `m_serverIn`, `m_stage`, (`m_currentTime` + `m_timeoutDuration`)) " +
                    "       ON DUPLICATE KEY UPDATE " +
                    "       `proxy`=VALUES(`proxy`), " +
                    "       `server`=VALUES(`server`), " +
                    "       `stage`=VALUES(`stage`), " +
                    "       `timeout`=VALUES(`timeout`);" +
                    "   SET `result` = TRUE; "+
                    "ELSE "+
                    "   SET `result` = FALSE; "+
                    " "+
                    "COMMIT; "+
                    "SELECT `result`; "+
                    " "+
                    "END$$ " +
                    "DELIMITER ;");

            createProcedure.execute();

            PreparedStatement releaseProcedure = connection.prepareStatement("" +
                    "DELIMITER $$ " +
                    "DROP PROCEDURE IF EXISTS `releaseHold` $$ " +
                    "CREATE PROCEDURE `releaseHold` " +
                    "(" +
                    "m_objectId VARCHAR(64), " +
                    "m_serverIn VARCHAR(32), " +
                    "m_proxyIn VARCHAR(32), " +
                    "m_currentTime BIGINT, " +
                    ")" +
                    "BEGIN " +
                    "DECLARE `m_serverName` VARCHAR(32); " +
                    "DECLARE `m_proxyName` VARCHAR(32); "+
                    "DECLARE `m_timeout` BIGINT; "+
                    "DECLARE `m_isHeld` BOOLEAN; "+
                    "DECLARE `m_isTimeout` BOOLEAN; " +
                    "DECLARE `result` BOOLEAN; "+
                    " "+
                    "START TRANSACTION; "+
                    " "+
                    "SELECT `timeout` INTO `m_timeout` FROM " + table + " WHERE `objectId`=m_objectId; "+
                    "SELECT `server` INTO `m_serverName` FROM "+ table + " WHERE `objectId`=m_objectId; "+
                    "SELECT `proxy` INTO `m_proxyName` FROM "+ table + " WHERE `objectId`=m_objectId; "+
                    " "+
                    "SET `m_serverName` = IF(`m_serverName` = NULL, `m_serverIn`, `m_serverName`); "+
                    "SET `m_proxyName` = IF(`m_serverName` = NULL, `m_proxyIn`, `m_proxyName`); "+
                    " "+
                    "SET `m_isHeld` IF(`m_serverIn` = `m_serverName` AND `m_proxyName` = `m_proxyIn`, true, false); " +
                    "SET `m_isTimeout` IF(`m_currentTime` >= `m_timeout`, true, false); " +

                    "IF `m_isHeld` OR `m_isTimeout` " +
                    "THEN "+
                    "   DELETE FROM "+table+" WHERE `objectId`=`m_objectId`;" +
                    "   SET `result` = TRUE; "+
                    "ELSE "+
                    "   SET `result` = FALSE; "+
                    " "+
                    "COMMIT; "+
                    "SELECT `result`; "+
                    " "+
                    "END$$ " +
                    "DELIMITER ;");
            releaseProcedure.execute();
        });
    }

    CompletableFuture<Boolean> isHeldServer(String server, String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `server` FROM "+table+" WHERE `objectId`=?");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1).equals(server);
            }else{
                return false;
            }
        });
    }

    CompletableFuture<Boolean> isHeldProxy(String proxy, String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `proxy` FROM "+table+" WHERE `objectId`=?");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1).equals(proxy);
            }else{
                return false;
            }
        });
    }

    CompletableFuture<String> getHoldingProxy(String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `proxy` FROM "+table+" WHERE `objectId`=?");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }else{
                return null;
            }
        });
    }

    CompletableFuture<String> getHoldingServer(String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `server` FROM "+table+" WHERE `objectId`=?");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }else{
                return null;
            }
        });
    }

    CompletableFuture<HoldData> getData(String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `proxy`, `server`, `stage`, `timeout` FROM "+table+" WHERE `objectId`=? FOR UPDATE");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String proxy = rs.getString(1);
                String server = rs.getString(2);
                String stageStr = rs.getString(3);
                long time = rs.getLong(4);
                try{
                    return new HoldData(objectId, proxy, server, SyncStage.valueOf(stageStr), time);
                }catch (IllegalArgumentException e) {
                    return null;
                }
            }else {
                return null;
            }
        });
    }

    CompletableFuture<SyncStage> getStage(String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `stage` FROM "+table+" WHERE `objectId`=? FOR UPDATE");
            ps.setString(1, objectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String stageStr = rs.getString(1);
                try{
                    return SyncStage.valueOf(stageStr);
                }catch (IllegalArgumentException e) {
                    return null;
                }
            }else {
                return SyncStage.UNLOAD;
            }
        });
    }

    /*
    * 1. SELECT로 정보 조회
    * 2. holdData 랑 대조
    * 3. 같으면 -> true
    * 4. 없으면 -> true / hold 데이터 INSERT
    * 5. timeout 이면 -> true / hold 데이터 INSERT
    * 6. 다르면 -> false
    * */
    CompletableFuture<Boolean> hold(String objectId, HoldData data, long timeoutMills){
        return database.executeAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                PreparedStatement ps2 = connection.prepareStatement("CALL `updateHold`(?, ?, ?, ?, ?, ?)");
                ps2.setString(1, objectId);
                ps2.setString(2, data.getProxy());
                ps2.setString(3, data.getServer());
                ps2.setLong(4, data.getTime());
                ps2.setString(5, data.getStage().name());
                ps2.setLong(6, timeoutMills);
                ps2.execute();
                connection.commit();
            }finally {
                connection.setAutoCommit(true);
            }
            return false;
        });
    }

    CompletableFuture<Void> holdUnsafe(String objectId, HoldData data){
        return database.executeAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                PreparedStatement ps2 = connection.prepareStatement("INSERT INTO "+table+" "+
                        "(`objectId`, `proxy`, `server`, `stage`, `timeout`) " +
                        "VALUES(?, ?, ?, ?, ?)" +
                        "ON DUPLICATE KEY UPDATE " +
                        "`proxy`=VALUES(`proxy`), " +
                        "`server`=VALUES(`server`), " +
                        "`stage`=VALUES(`stage`), " +
                        "`timeout`=VALUES(`timeout`);");
                ps2.setString(1, objectId);
                ps2.setString(2, data.getProxy());
                ps2.setString(3, data.getServer());
                ps2.setLong(4, data.getTime());
                ps2.setString(5, data.getStage().name());
                ps2.execute();
                connection.commit();
            }finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    CompletableFuture<Boolean> release(String objectId, HoldData data){
        return database.executeAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                PreparedStatement ps2 = connection.prepareStatement("CALL `releaseHold`(?, ?, ?, ?)");
                ps2.setString(1, objectId);
                ps2.setString(2, data.getProxy());
                ps2.setString(3, data.getServer());
                ps2.setLong(4, data.getTime());
                ps2.execute();
                connection.commit();
            }finally {
                connection.setAutoCommit(true);
            }
            return false;
        });
    }

    CompletableFuture<Void> releaseUnsafe(String objectId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `objectId`=?");
            ps.setString(1, objectId);
            ps.execute();
            return null;
        });
    }
}
