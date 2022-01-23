package com.minepalm.syncer.bootstrap;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bukkit.HelloBukkit;
import com.minepalm.syncer.core.Syncer;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import kr.travelrpg.travellibrary.bukkit.TravelLibraryBukkit;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class SyncerBukkit extends JavaPlugin {

    static Syncer syncer;

    public static Syncer inst(){
        return syncer;
    }

    @Override
    public void onEnable() {
        IConf conf = new BukkitConf(this);
        MySQLDatabase database = TravelLibraryBukkit.of().dataSource().mysql(conf.getMySQLName());
        HelloEveryone network = HelloBukkit.getMain();

        syncer = new Syncer(database, network);

        if(conf.requiredInitialization()) {
            syncer.initProcedures();
        }
    }


    protected static class BukkitConf extends SimpleConfig implements IConf{

        protected BukkitConf(JavaPlugin plugin) {
            super(plugin, "config.yml");
        }

        @Override
        public boolean requiredInitialization() {
            return config.getBoolean("initProcedure");
        }

        @Override
        public String getMySQLName() {
            return config.getString("TravelLibrary.mysql");
        }
    }
}
