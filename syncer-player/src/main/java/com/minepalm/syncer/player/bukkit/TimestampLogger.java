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
    boolean enabled;

    void setLog(boolean b){
        this.enabled = b;
    }

    public void log(String message){
        if(enabled)
            logger.info(format.format(new Date())+" "+message);
    }

    public void warn(String message){
        if(enabled)
            logger.warning(format.format(new Date())+" "+message);
    }

    public void log(Player player, String message){
        String playerFormat = player.getName()+"("+player.getUniqueId()+")";
        log("player: " + playerFormat+ " " + message);
    }
}
