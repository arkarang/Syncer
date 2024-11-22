package com.minepalm.syncer.player.bukkit.serialize;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class InvSerializer {

    private static Gson gson = new Gson();

    public static ItemStack[] deserialize(byte[] data) {
        String decompressed = ZstdCompressor.decompress(data);
        if(decompressed == null) {
            return new ItemStack[0];
        }
        return BukkitAdapter.getList(gson.fromJson(decompressed, JsonElement.class)).toArray(new ItemStack[0]);
    }

    public static byte[] serialize(ItemStack[] items) {
        return ZstdCompressor.compress(BukkitAdapter.toJson(Arrays.stream(items).toList()).toString());
    }

    public static byte[] serialize(List<ItemStack> items) {
        return ZstdCompressor.compress(BukkitAdapter.toJson(items).toString());
    }
}
