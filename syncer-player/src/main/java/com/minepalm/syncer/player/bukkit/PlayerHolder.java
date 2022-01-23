package com.minepalm.syncer.player.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerHolder {

    private final UUID uuid;

    Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public String toString(){
        return uuid.toString();
    }

}
