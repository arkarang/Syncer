package com.minepalm.syncer.player.bukkit;

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.player.MySQLLogger;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final PlayerSyncerConf conf;
    private final PlayerLoader loader;
    private final PlayerApplier applier;
    private final BukkitExecutor executor;

    private final AtomicBoolean allowed = new AtomicBoolean(false);
    private final Map<UUID, Boolean> eventPassed = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {

        if(!allowed.get()){
            event.setKickMessage(conf.getIllegalAccessText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        eventPassed.remove(event.getUniqueId());
        Bukkit.getLogger().warning(event.getUniqueId()+" fired AsyncPlayerPreLoginEvent");

        if(!event.isAsynchronous()){
            event.setKickMessage(conf.getIllegalAccessText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            loader.markPass(event.getUniqueId(), "SYNCHRONOUS_JOIN");
            return;
        }

        UUID uuid = event.getUniqueId();
        LoadResult completed = loader.load(uuid);

        if(completed.equals(LoadResult.SUCCESS)) {
            loader.unmark(uuid);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
            eventPassed.put(uuid, true);
        }else if(completed.equals(LoadResult.TIMEOUT)){
            event.setKickMessage(conf.getTimeoutText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            loader.markPass(event.getUniqueId(), "TIMEOUT");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoinEvent(PlayerJoinEvent event){
        Bukkit.getLogger().warning("player "+event.getPlayer().getName()+" fired PlayerJoinEvent");

        if(!eventPassed.containsKey(event.getPlayer().getUniqueId())){
            event.getPlayer().kickPlayer(conf.getIllegalAccessText());
            return;
        }

        boolean completed = loader.apply(event.getPlayer());

        if (!completed) {
            loader.markPass(event.getPlayer().getUniqueId(), "ILLEGAL_ACCESS");
            event.getPlayer().kickPlayer(conf.getIllegalAccessText());
        }
        eventPassed.remove(event.getPlayer().getUniqueId());
        executor.async(()->{
            MySQLLogger.log(PlayerDataLog.joinLog(applier.extract(event.getPlayer())));
        });
    }
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        loader.saveRuntime(event.getPlayer(), "QUIT");
        //manager.unregister(event.getPlayer().getUniqueId());
        executor.async(()->{
            MySQLLogger.log(PlayerDataLog.quitLog(applier.extract(event.getPlayer())));
        });
    }


    @EventHandler
    public void playerKick(PlayerKickEvent event) {
        loader.saveRuntime(event.getPlayer(), "KICK");
        //manager.unregister(event.getPlayer().getUniqueId());
    }

    public void setAllow(){
        allowed.set(true);
    }
}
