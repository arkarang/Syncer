package com.minepalm.syncer.player.bukkit.metadata;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.metadata.MetadataStoreBase;

import java.lang.reflect.Field;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class MetadataStoreInjector {

    private final Logger logger;

    public void inject(Server server, MetadataStoreBase<OfflinePlayer> target) {
        try {
            Class<CraftServer> clazz = CraftServer.class;
            Field storeField = clazz.getField("playerMetadata");
            storeField.setAccessible(true);
            storeField.set(server, target);
            logger.warning("overlapped PlayerMetadata to CustomPlayerMetadata. be careful!");
        }catch (Throwable e){
            logger.warning("failed inject PlayerMetadata cause: "+e.getClass().getSimpleName());
        }
    }
}
