package com.minepalm.syncer.player.bukkit;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataInventory {

    private static final int HELMET = 39, CHEST = 38, LEGGINGS = 37, BOOTS = 36, OFF_HAND = 40;

    private final ImmutableMap<Integer, ItemStack> items;

    private PlayerDataInventory(Map<Integer, ItemStack> items){
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

    public ItemStack getHelmet(){
        return this.items.get(HELMET);
    }

    public ItemStack getChest(){
        return this.items.get(CHEST);
    }

    public ItemStack getLeggings(){
        return this.items.get(LEGGINGS);
    }

    public ItemStack getBoots(){
        return this.items.get(BOOTS);
    }

    public ItemStack getOffHand(){
        return this.items.get(OFF_HAND);
    }

    public static PlayerDataInventory of(PlayerInventory playerInventory){
        HashMap<Integer, ItemStack> items = new HashMap<>();

        for(int i = 0; i < 36; i++){
            items.put(i, playerInventory.getItem(i).clone());
        }

        items.put(HELMET, playerInventory.getBoots().clone());
        items.put(CHEST, playerInventory.getChestplate().clone());
        items.put(LEGGINGS, playerInventory.getLeggings().clone());
        items.put(BOOTS, playerInventory.getBoots().clone());
        items.put(OFF_HAND, playerInventory.getItemInOffHand().clone());

        return new PlayerDataInventory(items);
    }

    public static PlayerDataInventory of(Map<Integer, ItemStack> map){
        return new PlayerDataInventory(map);
    }

    public ItemStack[] toArray(){
        ItemStack[] items = new ItemStack[40];
        for(int i = 0 ; i < 40; i++){
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

    public static PlayerDataInventory empty(){
        return new PlayerDataInventory(new HashMap<>());
    }

}
