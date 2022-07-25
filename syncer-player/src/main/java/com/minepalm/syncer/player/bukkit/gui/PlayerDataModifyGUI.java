package com.minepalm.syncer.player.bukkit.gui;

import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

public class PlayerDataModifyGUI extends PlayerDataGUI {

    private final MySQLPlayerInventoryDataModel database;

    public PlayerDataModifyGUI(UUID uuid, ItemStack[] items, MySQLPlayerInventoryDataModel database) {
        super(uuid, items);
        this.database = database;

        cancelled.put(44, true);
        ItemStack commit = new ItemStack(Material.BARRIER);
        ItemMeta meta = commit.getItemMeta();
        meta.setDisplayName("§c인벤토리 적용하기");
        meta.setLore(Collections.singletonList("§f클릭시 인벤토리를 저장합니다. 주의."));
        commit.setItemMeta(meta);

        inv.setItem(44, commit);
        funcs.put(44, event -> {
            this.database.save(target, toData()  );
            event.getWhoClicked().closeInventory();
        });
    }

}
