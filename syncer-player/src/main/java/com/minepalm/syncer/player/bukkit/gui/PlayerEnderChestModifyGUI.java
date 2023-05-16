package com.minepalm.syncer.player.bukkit.gui;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.syncer.player.data.PlayerDataEnderChest;
import com.minepalm.syncer.player.data.PlayerDataInventory;
import com.minepalm.syncer.player.mysql.MySQLPlayerEnderChestDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerEnderChestModifyGUI extends ArkarangGUI {

    private final MySQLPlayerEnderChestDataModel database;
    private final UUID target;

    public PlayerEnderChestModifyGUI(UUID uuid, ItemStack[] items, MySQLPlayerEnderChestDataModel database) {
        super(5, title(uuid));
        this.target = uuid;
        for(int i = 0 ; i < items.length ; i++){
            inv.setItem(i, items[i]);
        }
        for(int i = 0 ; i < 54 ; i++)
            this.cancelled.put(i, false );
        this.database = database;

        cancelled.put(44, true);
        ItemStack commit = new ItemStack(Material.BARRIER);
        ItemMeta meta = commit.getItemMeta();
        meta.setDisplayName("§c인벤토리 적용하기");
        meta.setLore(Collections.singletonList("§f클릭시 인벤토리를 저장합니다. 주의."));
        commit.setItemMeta(meta);

        inv.setItem(44, commit);
        funcs.put(44, event -> {
            this.database.save(target, toData());
            event.getWhoClicked().closeInventory();
        });
    }

    public PlayerDataEnderChest toData(){
        Map<Integer, ItemStack> map = new HashMap<>();
        for(int i = 0 ; i < 36 ; i++){
            ItemStack item = Optional.ofNullable(inv.getItem(i)).orElse(new ItemStack(Material.AIR  ));
            map.put(i, item);
        }
        return PlayerDataEnderChest.of(map);
    }

    private static String title(UUID uuid){
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        if(off != null){
            return off.getName()+"("+uuid+") EnderChest";
        }else{
            return uuid.toString();
        }
    }
}
