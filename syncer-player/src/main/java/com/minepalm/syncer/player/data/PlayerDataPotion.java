package com.minepalm.syncer.player.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Data
@AllArgsConstructor
public class PlayerDataPotion {

    public Collection<PotionEffect> effects;

    private static class PotionTypeAdapter extends TypeAdapter<PotionEffectType> {

        @Override
        public void write(JsonWriter writer, PotionEffectType type) throws IOException {
            if(type == null) {
                nullSafe().write(writer, null);
            }else {
                writer.beginObject();
                writer.name("type").value(type.getKey().toString());
                writer.endObject();
            }

        }

        @Override
        public PotionEffectType read(JsonReader reader) throws IOException {
            try {
                reader.beginObject();
                if (reader.nextName().equals("type")) {
                    return PotionEffectType.getByKey(NamespacedKey.fromString(reader.nextString()));
                } else
                    return nullSafe().read(reader);
            }finally {
                reader.endObject();
            }
        }
    }

    private static Gson gson;

    static{
        gson = new GsonBuilder()
                .registerTypeAdapter(PotionEffectType.class, new PotionTypeAdapter())
                .create();
    }

    public String toJson(){
        return gson.toJson(this);
    }

    public static PlayerDataPotion from(String json){
        return gson.fromJson(json, PlayerDataPotion.class);
    }

    public static PlayerDataPotion empty(){
        return new PlayerDataPotion(new ArrayList<>());
    }
}
