package com.minepalm.syncer.player.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

import java.util.*;

public class PlayerFreezer implements Listener {

    private static final Map<UUID, Long> frozenPlayers = new HashMap<>();

    public static void freezePlayer(UUID uuid) {
        frozenPlayers.put(uuid, System.currentTimeMillis()+ 5000L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        long time = frozenPlayers.getOrDefault(event.getPlayer().getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(event.getPlayer().getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        long time = frozenPlayers.getOrDefault(player.getUniqueId(), 0L);
        if (time <= System.currentTimeMillis()) {
            frozenPlayers.remove(player.getUniqueId());
        }
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        frozenPlayers.remove(event.getPlayer().getUniqueId());
    }
}
