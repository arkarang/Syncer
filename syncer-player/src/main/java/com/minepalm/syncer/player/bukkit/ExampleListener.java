package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class ExampleListener implements Listener {

    private final PlayerDataStorage storage;
    private final PlayerModifier modifier;
    private final SyncService service;
    private final ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();

    @EventHandler
    public void asyncPlayerJoin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {
        UUID uuid = event.getUniqueId();
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        cachedData.remove(uuid);

        try {
            boolean acquired = synced.hold(Duration.ofMillis(5000L));

            if(acquired) {
                PlayerData data = storage.getPlayerData(uuid).get();
                cachedData.put(uuid, data);

                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
            }
        }catch (TimeoutException e){

        }

        //todo: execute timeout logic
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerLoginEvent(PlayerJoinEvent event){
        if(cachedData.containsKey(event.getPlayer().getUniqueId())){
            modifier.apply(event.getPlayer(), cachedData.get(event.getPlayer().getUniqueId()));
        }else{
            //todo: set illegal Access message;
            event.getPlayer().kickPlayer("TIMEOUT");
        }
    }
}
