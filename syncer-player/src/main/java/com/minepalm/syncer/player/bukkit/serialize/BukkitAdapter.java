package com.minepalm.syncer.player.bukkit.serialize;

import com.google.gson.JsonElement;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class BukkitAdapter {

    public static JsonElement toJson(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        return ItemStackSerializer.toJson(nms);
    }

    public static ItemStack getItem(JsonElement element) {
        net.minecraft.world.item.ItemStack nms = ItemStackSerializer.getItem(element);
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static JsonElement toJson(Collection<ItemStack> items) {
        return ItemStackSerializer.toJson(items.stream().map(CraftItemStack::asNMSCopy).toList());
    }

    public static List<ItemStack> getList(JsonElement element) {
        return ItemStackSerializer.getList(element).stream().map(CraftItemStack::asBukkitCopy).toList();
    }
}
