package com.minepalm.syncer.player.bukkit.gui;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.syncer.player.data.PlayerDataInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataGUI extends ArkarangGUI {

    UUID target;


    public PlayerDataGUI(UUID uuid, ItemStack[] items) {
        super(5, title(uuid));
        this.target = uuid;
        for(int i = 0 ; i < items.length ; i++){
            inv.setItem(i, items[i]);
        }
        for(int i = 0 ; i < 54 ; i++)
            this.cancelled.put(i, false );

    }

    public PlayerDataInventory toData(){
        Map<Integer, ItemStack> map = new HashMap<>();
        for(int i = 0 ; i < 41 ; i++){
            ItemStack item = Optional.ofNullable(inv.getItem(i)).orElse(new ItemStack(Material.AIR  ));
            map.put(i, item);
        }
        return PlayerDataInventory.of(map);
    }

    private static String title(UUID uuid){
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        if(off != null){
            return off.getName()+"("+uuid+")";
        }else{
            return uuid.toString();
        }
    }
}
