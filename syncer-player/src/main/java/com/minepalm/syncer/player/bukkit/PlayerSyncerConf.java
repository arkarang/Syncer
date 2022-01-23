package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerSyncerConf extends SimpleConfig {

    protected PlayerSyncerConf(JavaPlugin plugin, String fileName) {
        super(plugin, fileName);
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

    public String getMySQLName(){
        return config.getString("TravelLibrary.mysql");
    }

}
