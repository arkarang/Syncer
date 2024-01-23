package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerDataPotion;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

public class SetPotion implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.getPotionEffects() != null) {
            List<PotionEffect> potions = player.getActivePotionEffects().stream().collect(Collectors.toList());
            potions.forEach(potion -> player.removePotionEffect(potion.getType()));
            player.addPotionEffects(data.getPotionEffects().getEffects());
        }
    }

}
