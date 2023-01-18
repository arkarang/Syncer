package com.minepalm.syncer.player.bukkit.strategies;

import com.minepalm.syncer.player.data.PlayerData;
import org.bukkit.entity.Player;

public class SetPotion implements ApplyStrategy {

    @Override
    public void applyPlayer(Player player, PlayerData data) {
        if(data.potions() != null) {
            var potions = player.getActivePotionEffects().stream().toList();
            potions.forEach(potion -> player.removePotionEffect(potion.getType()));
            player.addPotionEffects(data.potions().getEffects());
        }
    }

}
