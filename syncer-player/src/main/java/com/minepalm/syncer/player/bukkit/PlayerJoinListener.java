package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.player.MySQLLogger;
import lombok.RequiredArgsConstructor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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


    private void makeDelayForTest(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private UUID arkarang = UUID.fromString("7ce52d57-8ac3-4daa-b9a1-5c3c0f8aadf4");
    private void testCode(UUID uuid) {
        if(uuid.equals(arkarang)) {
            String currentServer = PlayerSyncer.getInst().syncer.getHolderRegistry().getLocalName();
            if(currentServer.equalsIgnoreCase("RPG-dungeon1-4")) {
                makeDelayForTest(10000);
            } else if ( currentServer.equalsIgnoreCase("RPG-dungeon1-5")) {
                makeDelayForTest(5000);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {
        //testCode(event.getUniqueId());
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
            loader.unmark(uuid);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }else if(completed.equals(LoadResult.TIMEOUT)){
            event.setKickMessage(conf.getTimeoutText());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
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
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = applier.extract(event.getPlayer());

        loader.saveAsync(uuid, data, "QUIT");
        loop.quit(event.getPlayer().getUniqueId());

        executor.async(() -> {
            MySQLLogger.log(PlayerDataLog.quitLog(data));
        });

    }

    public void setAllow(){
        allowed.set(true);
    }
}
