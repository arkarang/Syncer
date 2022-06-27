package com.minepalm.syncer.player.bukkit;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PlayerDataValues {

    private final double health;
    private final double healthScale;
    private final int level;
    private final int foodLevel;
    private final float exp;
    private final float saturation;
    private final float exhaustion;
    private final int heldSlot;
    private final int gamemode;
    private final boolean fly;

    public static PlayerDataValues getDefault(){
        return new PlayerDataValues(20, 0d,0, 20, 0, 0f, 0f, 0, 0, false);
    }

}
