package com.minepalm.syncer.player.bukkit;

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.player.MySQLLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

    private final UpdateTimeoutLoop loop;

    private final AtomicBoolean allowed = new AtomicBoolean(false);
    private final Map<UUID, Boolean> eventPassed = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {

        if(!allowed.get()){
            event.setKickMessage(conf.getIllegalAccessText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());

        if(player != null && player.isBanned()){
            return;
        }

        eventPassed.remove(event.getUniqueId());

        if(!event.isAsynchronous()){
            event.setKickMessage(conf.getIllegalAccessText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
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
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoinEvent(PlayerJoinEvent event){

        boolean completed = loader.apply(event.getPlayer());
        loop.join(event.getPlayer().getUniqueId());

        if (!completed) {
            event.getPlayer().kickPlayer(conf.getIllegalAccessText());
        }

        executor.async(()->{
            MySQLLogger.log(PlayerDataLog.joinLog(applier.extract(event.getPlayer())));
        });
    }
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        loader.saveRuntime(event.getPlayer(), "QUIT");
        loop.quit(event.getPlayer().getUniqueId());
        executor.async(() -> {
            MySQLLogger.log(PlayerDataLog.quitLog(applier.extract(event.getPlayer())));
        });

    }


    @EventHandler
    public void playerKick(PlayerKickEvent event) {
        loader.saveRuntime(event.getPlayer(), "KICK");
        loop.quit(event.getPlayer().getUniqueId());
        executor.async(()->{
            MySQLLogger.log(PlayerDataLog.kickLog(applier.extract(event.getPlayer())));
        });
    }

    public void setAllow(){
        allowed.set(true);
    }
}
