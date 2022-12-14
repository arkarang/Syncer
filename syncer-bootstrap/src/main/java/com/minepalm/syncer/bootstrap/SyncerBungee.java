package com.minepalm.syncer.bootstrap;

import com.minepalm.arkarangutils.bungee.BungeeConfig;
import com.minepalm.library.PalmLibrary;
import com.minepalm.library.network.api.PalmNetwork;
import com.minepalm.syncer.core.Syncer;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class SyncerBungee extends Plugin implements Listener {

    static Syncer syncer;

    public static Syncer inst(){
        return syncer;
    }

    @Override
    public void onEnable(){
        IConf conf = new BungeeConf(this);
        var database = PalmLibrary.INSTANCE.getDataSource().mysql(conf.getMySQLName()).java();
        PalmNetwork network = PalmLibrary.INSTANCE.getNetwork();

        syncer = new Syncer(database, network);

        if(conf.requiredInitialization()) {
            syncer.initProcedures();
        }
    }

    @Override
    public void onDisable(){

    }

    @EventHandler
    public void onPlayerDisconnected(ServerDisconnectEvent event){
        syncer.getHolderRegistry().getHolder(event.getPlayer().getUniqueId().toString());

    }

    protected static class BungeeConf extends BungeeConfig implements IConf{

        public BungeeConf(Plugin plugin) {
            super(plugin, "config.yml", true);
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
