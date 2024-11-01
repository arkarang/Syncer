package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.player.MySQLLogger;
import lombok.RequiredArgsConstructor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final String currentServer;

    private final PlayerSyncerConf conf;
    private final PlayerLoader loader;
    private final PlayerApplier applier;
    private final BukkitExecutor executor;

    private final AtomicBoolean allowed = new AtomicBoolean(false);
    private final Set<UUID> sucessfullyLoaded = new HashSet<>();


    private void makeDelayForTest(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {
        //testCode(event.getUniqueId());
        sucessfullyLoaded.remove(event.getUniqueId());
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());

        if(player != null && player.isBanned()){
            String reason = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(player.getName()).getReason();
            event.setKickMessage(reason);
            return;
        }

        if(Bukkit.hasWhitelist() && player != null && !player.isWhitelisted()){
            event.setKickMessage(conf.getWhitelistText());
            return;
        }

        event.setKickMessage(conf.getIllegalAccessText());

        if(!event.isAsynchronous()){
            return;
        }

        if(!allowed.get()){
            return;
        }

        UUID uuid = event.getUniqueId();
        LoadResult completed = loader.load(uuid);

        if(completed.equals(LoadResult.SUCCESS)) {
            loader.invalidateUpdatedTime(uuid);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }else {
            event.setKickMessage(conf.getTimeoutText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoinEvent(PlayerJoinEvent event){
        boolean completed = loader.apply(event.getPlayer());

        if (!completed) {
            event.getPlayer().kickPlayer(conf.getIllegalAccessText());
            return;
        }

        sucessfullyLoaded.add(event.getPlayer().getUniqueId());

        executor.async(()->{
            MySQLLogger.log(PlayerDataLog.joinLog(applier.extract(event.getPlayer())));
        });
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = applier.extract(event.getPlayer());

        if(loader.isSuccessfullyLoaded(event.getPlayer().getUniqueId())) {
            loader.savePlayerQuit(uuid, data, "logout - "+currentServer);
        }

        executor.async(() -> {
            MySQLLogger.log(PlayerDataLog.quitLog(data));
        });

        loader.invalidateLoaded(uuid);
    }

    public void setAllow(){
        allowed.set(true);
    }
}
