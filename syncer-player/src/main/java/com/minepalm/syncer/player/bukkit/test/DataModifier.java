package com.minepalm.syncer.player.bukkit.test;

import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@RequiredArgsConstructor
public class DataModifier {

    final PlayerDataInventory inventory;

    public PlayerDataInventory setItem(int index, ItemStack item){
        Map<Integer, ItemStack> map = inventory.getItems();
        map.put(index, item);
        return PlayerDataInventory.of(map);
    }

    public PlayerDataInventory moveItem(int indexFrom, int indexTo){
        Map<Integer, ItemStack> map = inventory.getItems();
        ItemStack item = map.get(indexFrom);
        ItemStack item2 = map.get(indexTo);
        map.put(indexTo, item);
        map.put(indexFrom, item2);
        return PlayerDataInventory.of(map);
    }

    public static boolean assertEquals(PlayerDataInventory data1, PlayerDataInventory data2){
        if(data1 == null || data2 == null){
            return false;
        }
        Map<Integer, ItemStack> dataMap1, dataMap2;
        dataMap1 = data1.getItems();
        dataMap2 = data2.getItems();
        if(dataMap1.size() != dataMap2.size()){
            return false;
        }
        for (Integer key : dataMap1.keySet()) {
            if(!dataMap2.containsKey(key)){
                return false;
            }
            ItemStack item1, item2;
            item1 = dataMap1.get(key);
            item2 = dataMap2.get(key);
            if(!item1.isSimilar(item2)){
                return false;
            }
        }

        return true;
    }
}
