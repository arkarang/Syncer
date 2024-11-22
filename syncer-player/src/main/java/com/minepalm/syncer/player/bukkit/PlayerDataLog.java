package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.player.bukkit.serialize.InvSerializer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PlayerDataLog {

    public final String task_name;
    public final UUID uuid;
    public final byte[] inventoryData;
    public final byte[] enderChestData;
    public final long log_generated_time;
    public final long data_generated_time;

    public static PlayerDataLog loadLog(PlayerData data){
        String taskName = "LOAD";
        return toLog(data, taskName);
    }

    public static PlayerDataLog saveLog(PlayerData data){
        String taskName = "SAVE";
        return toLog(data, taskName);
    }

    public static PlayerDataLog saveTimeout(PlayerData data){
        String taskName = "TIMEOUT_SAVE";
        return toLog(data, taskName);
    }

    public static PlayerDataLog inject(PlayerData data){
        String taskName = "INJECT";
        return toLog(data, taskName);
    }

    public static PlayerDataLog apply(PlayerData data){
        String taskName = "APPLY";
        return toLog(data, taskName);
    }

    public static PlayerDataLog injectRollback(PlayerData data){
        String taskName = "ROLLBACK_INJECT";
        return toLog(data, taskName);
    }

    public static PlayerDataLog joinLog(PlayerData data){
        String taskName = "JOIN";
        return toLog(data, taskName);
    }

    public static PlayerDataLog quitLog(PlayerData data){
        String taskName = "QUIT";
        return toLog(data, taskName);
    }

    public static PlayerDataLog kickLog(PlayerData data){
        String taskName = "KICK";
        return toLog(data, taskName);
    }

    public static PlayerDataLog dump(String reason, PlayerData data){
        return toLog(data, reason);
    }


    public static PlayerDataLog applyNull(UUID uuid){
        return nullLog(uuid, "APPLY_NULL");
    }

    public static PlayerDataLog duplicateSaveLog(PlayerData data){
        return toLog(data, "DUPLICATE_SAVE_DETECT");
    }

    public static PlayerDataLog nullLog(UUID uuid, String taskName){
        byte[] inventoryData = InvSerializer.serialize(new ItemStack[0]);
        byte[] enderChestData = InvSerializer.serialize(new ItemStack[0]);
        long dataGeneratedTime = 0;
        long logGeneratedTime = System.currentTimeMillis();

        return new PlayerDataLog(taskName, uuid, inventoryData, enderChestData, logGeneratedTime, dataGeneratedTime);
    }


    @NotNull
    private static PlayerDataLog toLog(PlayerData data, String taskName) {
        UUID uuid = data.getUuid();
        byte[] inventoryData;
        byte[] enderChestData;
        long dataGeneratedTime;

        if(data.getInventory() != null){
            inventoryData = InvSerializer.serialize(data.getInventory().toArray());
            dataGeneratedTime = data.getInventory().getGeneratedTime();
        }else{
            inventoryData = InvSerializer.serialize(new ItemStack[0]);
            dataGeneratedTime = 0;
        }

        if(data.getEnderChest() != null){
            enderChestData = InvSerializer.serialize(data.getEnderChest().toArray());
        }else{
            enderChestData = InvSerializer.serialize(new ItemStack[0]);
        }

        long logGeneratedTime = System.currentTimeMillis();

        return new PlayerDataLog(taskName, uuid, inventoryData, enderChestData, logGeneratedTime, dataGeneratedTime);
    }
}
