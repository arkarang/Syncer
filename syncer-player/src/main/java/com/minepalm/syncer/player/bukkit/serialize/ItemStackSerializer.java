package com.minepalm.syncer.player.bukkit.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class ItemStackSerializer {

    public static JsonElement toJson(ItemStack itemStack) {
        CompoundTag nbt = itemStack.save(new CompoundTag());
        DynamicOps<Tag> nbtOps = NbtOps.INSTANCE;
        Dynamic<Tag> dynamic = new Dynamic<>(nbtOps, nbt);
        return dynamic.convert(JsonOps.INSTANCE).getValue();
    }

    public static ItemStack getItem(JsonElement element) {
        Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, element);
        Tag nbt = dynamic.convert(NbtOps.INSTANCE).getValue();
        return ItemStack.of((CompoundTag)nbt);
    }

    public static JsonElement toJson(Collection<ItemStack> item) {
        JsonArray array = new JsonArray();
        ItemStack[] items = item.toArray(new ItemStack[0]);
        for (int i = 0; i < item.size(); i++) {
            array.add(toJson(items[i]));
        }
        return array;
    }

    public static Collection<ItemStack> getList(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        ItemStack[] items = new ItemStack[array.size()];
        for (int i = 0; i < array.size(); i++) {
            items[i] = getItem(array.get(i));
        }
        return List.of(items);
    }
}