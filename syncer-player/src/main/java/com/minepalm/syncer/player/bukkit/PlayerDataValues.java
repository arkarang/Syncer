package com.minepalm.syncer.player.bukkit;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PlayerDataValues {

    private final double health;
    private final int level;
    private final int foodLevel;
    private final float exp;
    private final float saturation;
    private final float exhaustion;
    private final int heldSlot;

    public static PlayerDataValues getDefault(){
        return new PlayerDataValues(20, 0, 20, 0, 0f, 0f, 0);
    }

}
