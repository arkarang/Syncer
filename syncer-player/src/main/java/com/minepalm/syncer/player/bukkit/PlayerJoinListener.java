package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.PlayerTransactionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final PlayerSyncerConf conf;
    private final PlayerLoader loader;

    private final PlayerTransactionManager manager;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {

        if(!event.isAsynchronous()){
            event.setKickMessage(conf.getIllegalAccessText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            loader.markPass(event.getUniqueId(), "SYNCHRONOUS_JOIN");
        }

        UUID uuid = event.getUniqueId();
        LoadResult completed = loader.load(uuid);

        if(completed.equals(LoadResult.SUCCESS)) {
            loader.unmark(uuid);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);

        }else if(completed.equals(LoadResult.TIMEOUT)){
            event.setKickMessage(conf.getTimeoutText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            loader.markPass(event.getUniqueId(), "TIMEOUT");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerJoinEvent(PlayerJoinEvent event){
        boolean completed = loader.apply(event.getPlayer());

        if (!completed) {
            loader.markPass(event.getPlayer().getUniqueId(), "ILLEGAL_ACCESS");
            event.getPlayer().kickPlayer(conf.getIllegalAccessText());
        }
    }
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        loader.saveRuntime(event.getPlayer());
        //manager.unregister(event.getPlayer().getUniqueId());
    }


    @EventHandler
    public void playerKick(PlayerKickEvent event) {
        loader.saveRuntime(event.getPlayer());
        //manager.unregister(event.getPlayer().getUniqueId());
    }
}
