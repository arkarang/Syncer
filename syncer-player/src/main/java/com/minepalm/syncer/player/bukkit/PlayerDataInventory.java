package com.minepalm.syncer.player.bukkit;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class PlayerDataInventory {

    private static final int HELMET = 39, CHEST = 38, LEGGINGS = 37, BOOTS = 36, OFF_HAND = 40;
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final ImmutableMap<Integer, ItemStack> items;
    private final long generatedTime;

    public long getGeneratedTime() {
        return generatedTime;
    }

    private PlayerDataInventory(Map<Integer, ItemStack> items){
        this.items = ImmutableMap.copyOf(items);
        this.generatedTime = System.currentTimeMillis();
    }

    private PlayerDataInventory(Map<Integer, ItemStack> items, long generatedTime){
        this.items = ImmutableMap.copyOf(items);
        this.generatedTime = generatedTime;
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
            items.add(this.items.getOrDefault(i, AIR));
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
            ItemStack item = playerInventory.getItem(i);
            if(item != null) {
                items.put(i, item.clone());
            }else{
                items.put(i, AIR);
            }
        }

        items.put(HELMET, Optional.ofNullable(playerInventory.getHelmet()).orElse(AIR).clone());
        items.put(CHEST, Optional.ofNullable(playerInventory.getChestplate()).orElse(AIR).clone());
        items.put(LEGGINGS, Optional.ofNullable(playerInventory.getLeggings()).orElse(AIR).clone());
        items.put(BOOTS, Optional.ofNullable(playerInventory.getBoots()).orElse(AIR).clone());
        items.put(OFF_HAND, Optional.ofNullable(playerInventory.getItemInOffHand()).orElse(AIR).clone());

        return new PlayerDataInventory(items);
    }

    public PlayerDataInventory copy(){
        return new PlayerDataInventory(this.items, System.currentTimeMillis());
    }

    public static PlayerDataInventory of(Map<Integer, ItemStack> map){
        return new PlayerDataInventory(map);
    }

    public static PlayerDataInventory of(Map<Integer, ItemStack> map, long generatedTime){
        return new PlayerDataInventory(map, generatedTime);
    }

    public ItemStack[] toArray(){
        ItemStack[] items = new ItemStack[41];
        for(int i = 0 ; i < 41; i++){
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
