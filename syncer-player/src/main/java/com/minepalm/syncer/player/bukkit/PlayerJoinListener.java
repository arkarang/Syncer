package com.minepalm.syncer.player.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final PlayerLoader loader;

    @EventHandler(priority = EventPriority.LOWEST)
    public void asyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws ExecutionException, InterruptedException {
        UUID uuid = event.getUniqueId();
        boolean completed = loader.load(uuid);

        if(completed) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }else{
            event.setKickMessage("cannot acquire player lock! uuid: "+event.getUniqueId());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoinEvent(PlayerJoinEvent event){
        boolean completed = loader.apply(event.getPlayer());

        if(!completed){
            event.getPlayer().kickPlayer("SYNCER: TIMEOUT");
        }

    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        loader.save(event.getPlayer());
    }

}
