package com.minepalm.syncer.bootstrap;

import com.minepalm.arkarangutils.bungee.BungeeConfig;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bungee.HelloBungee;
import com.minepalm.syncer.core.Syncer;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import kr.travelrpg.travellibrary.bungee.TravelLibraryBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class SyncerBungee extends Plugin implements Listener {

    static Syncer syncer;

    public static Syncer inst(){
        return syncer;
    }

    @Override
    public void onEnable(){
        IConf conf = new BungeeConf(this);
        MySQLDatabase database = TravelLibraryBungee.of().dataSource().mysql(conf.getMySQLName());
        HelloEveryone network = HelloBungee.getMain();

        syncer = new Syncer(database, network);
        syncer.register(UUID.class, new UUIDHolder());

        if(conf.requiredInitialization()) {
            syncer.initProcedures();
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable(){

    }

    @EventHandler
    public void onPlayerDisconnected(PlayerDisconnectEvent event){
        try {
            syncer.of(event.getPlayer().getUniqueId()).unsafe().release();
        }catch (Exception e){
            e.printStackTrace();
        }
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
