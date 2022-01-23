package com.minepalm.syncer.player.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TimestampLogger {
    private final Logger logger;
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    public void log(String message){
        logger.info(format.format(new Date())+" "+message);
    }

    public void log(Player player, String message){
        String playerFormat = player.getName()+"("+player.getUniqueId()+")";
        log("player: " + playerFormat+ " " + message);
    }
}
