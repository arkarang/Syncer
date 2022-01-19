package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ExampleListener implements Listener {

    private final PlayerDataStorage storage;
    private final PlayerModifier modifier;
    private final SyncService service;
    private final BukkitExecutor executor;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();

    @EventHandler
    public void asyncPlayerJoin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {
        UUID uuid = event.getUniqueId();
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        cachedData.remove(uuid);

        try {
            synced.hold(Duration.ofMillis(5000L));
            PlayerData data = storage.getPlayerData(uuid).get();
            cachedData.put(uuid, data);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
            synced.release();
        }catch (TimeoutException e){

        }

        //todo: execute timeout logic
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerLoginEvent(PlayerJoinEvent event){
        if(cachedData.containsKey(event.getPlayer().getUniqueId())){
            UUID uuid = event.getPlayer().getUniqueId();
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);
            modifier.apply(event.getPlayer(), cachedData.get(event.getPlayer().getUniqueId()));
        }else{
            //todo: set illegal Access message;
            event.getPlayer().kickPlayer("TIMEOUT");
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        executor.async(()->{
            UUID uuid = event.getPlayer().getUniqueId();
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            try {
                synced.hold(Duration.ofMillis(5000L));
                PlayerData data = modifier.extract(event.getPlayer());
                storage.save(uuid, data);
            }catch (InterruptedException ignored){

            }catch (ExecutionException e){
                e.printStackTrace();
            }catch (TimeoutException e){
                logger.info("saving player inventory of "+event.getPlayer().getName()+" have timeout");
            }finally {
                synced.release();
            }
        });
    }
}
