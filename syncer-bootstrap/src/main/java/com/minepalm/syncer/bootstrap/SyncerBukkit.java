package com.minepalm.syncer.bootstrap;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import com.minepalm.library.PalmLibrary;
import com.minepalm.library.network.api.PalmNetwork;
import com.minepalm.syncer.core.Syncer;
import org.bukkit.plugin.java.JavaPlugin;

public class SyncerBukkit extends JavaPlugin {

    static Syncer syncer;

    public static Syncer inst(){
        return syncer;
    }

    @Override
    public void onEnable() {
        IConf conf = new BukkitConf(this);
        var database = PalmLibrary.getDataSource().mysql(conf.getMySQLName()).java();
        PalmNetwork network = PalmLibrary.getNetwork();

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
            return config.getString("mysql");
        }
    }
}
