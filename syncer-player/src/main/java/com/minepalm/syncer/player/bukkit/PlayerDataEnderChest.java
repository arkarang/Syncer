package com.minepalm.syncer.player.bukkit;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataEnderChest {

    private final ImmutableMap<Integer, ItemStack> items;

    private PlayerDataEnderChest(Map<Integer, ItemStack> items){
        this.items = ImmutableMap.copyOf(items);
    }

    public Map<Integer, ItemStack> getItems(){
        HashMap<Integer, ItemStack> map = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            map.put(entry.getKey(), entry.getValue().clone());
        }
        return map;
    }

    public List<ItemStack> getStorageItems(){
        List<ItemStack> items = new ArrayList<>();
        for(int i = 0; i < 36; i++){
            if(this.items.containsKey(i)){
                items.add(this.items.get(i));
            }else{
                items.add(new ItemStack(Material.AIR));
            }
        }
        return items;
    }

    public static PlayerDataEnderChest of(Inventory enderChest){
        HashMap<Integer, ItemStack> items = new HashMap<>();

        for(int i = 0; i < 36; i++){
            items.put(i, enderChest.getItem(i).clone());
        }

        return new PlayerDataEnderChest(items);
    }

    public static PlayerDataEnderChest of(Map<Integer, ItemStack> map){
        return new PlayerDataEnderChest(map);
    }

    public ItemStack[] toArray(){
        ItemStack[] items = new ItemStack[36];
        for(int i = 0 ; i < 36; i++){
            ItemStack item = null;
            if(this.items.containsKey(i)){
                item = this.items.get(i);
            }
            if(item == null){
                item = new ItemStack(Material.AIR);
            }
            items[i] = item;
        }
        return items;
    }

    public static PlayerDataEnderChest empty(){
        return new PlayerDataEnderChest(new HashMap<>());
    }

}
