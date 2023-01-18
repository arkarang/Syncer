package com.minepalm.syncer.player.test;

import com.google.gson.Gson;
import com.minepalm.syncer.player.data.PlayerDataPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PotionSerializeTest {

    @Test
    @Ignore
    public void test(){
        List<PotionEffect> effects = new ArrayList<>();
        for(int i = 0; i < 10; i ++){

            System.out.println("added: "+i);
            effects.add(new PotionEffect(PotionEffectType.BLINDNESS, i+4, i+2));
        }
        PlayerDataPotion potion = new PlayerDataPotion(effects);
        String serialized = potion.toJson();
        System.out.println("seriailzed: "+serialized);
        PlayerDataPotion deserialized = PlayerDataPotion.from(serialized);
        System.out.println("deserialized: "+deserialized);
    }
}
