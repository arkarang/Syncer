package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerSyncerConf extends SimpleConfig {

    @Getter
    private final String timeoutText, illegalAccessText, whitelistText;

    protected PlayerSyncerConf(JavaPlugin plugin, String fileName) {
        super(plugin, fileName);
        timeoutText = config.getString("Message.KICK_TIMEOUT").replace("&", "§");
        illegalAccessText = config.getString("Message.KICK_ILLEGAL_JOIN").replace("&", "§");
        whitelistText = config.getString("Message.WHITELIST", "&6&l[렌독] &f현재 서버 점검으로 인해 접속이 불가능합니다.").replace("&", "§");
    }

    public List<String> getStrategies(){
        return config.getStringList("flags");
    }

    public long getTimeout(){
        return config.getLong("joinTimeoutMills");
    }

    public long getExtendingTimeoutPeriod(){
        return config.getLong("extendingTimeoutPeriodMills");
    }

    public long getSavePeriod(){
        return config.getLong("savePeriodMills", 5000L*12*10);
    }

    public String getMySQLName(){
        return config.getString("TravelLibrary.inventory");
    }

    public String getLogMySQLName(){
        return config.getString("TravelLibrary.log");
    }

    public boolean logResults(){
        return config.getBoolean("logResult");
    }
}
