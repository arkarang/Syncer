package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.bungeejump.impl.BungeeJump;
import com.minepalm.bungeejump.impl.bukkit.BungeeJumpBukkit;
import com.minepalm.syncer.api.SyncToken;
import com.minepalm.syncer.bootstrap.SyncerBukkit;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.player.bukkit.strategies.*;
import com.minepalm.syncer.player.mysql.MySQLPlayerEnderChestDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerValuesDataModel;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import kr.travelrpg.travellibrary.bukkit.TravelLibraryBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class PlayerSyncer extends JavaPlugin {

    Syncer syncer;
    BungeeJump bungeeJump;
    UpdateTimeoutLoop loop;
    PlayerJoinListener listener;
    PlayerDataStorage storage;
    PlayerApplier modifier;
    PlayerLoader loader;

    @Override
    public void onEnable() {
        PlayerSyncerConf conf = new PlayerSyncerConf(this, "config.yml");
        this.syncer = SyncerBukkit.inst();
        this.bungeeJump = BungeeJumpBukkit.getService();
        MySQLDatabase database = TravelLibraryBukkit.of().dataSource().mysql(conf.getMySQLName());
        BukkitExecutor bukkitExecutor = new BukkitExecutor(this, Bukkit.getScheduler());

        MySQLPlayerEnderChestDataModel enderChestDataModel = new MySQLPlayerEnderChestDataModel("playersyncer_enderchest", database);
        MySQLPlayerInventoryDataModel inventoryDataModel = new MySQLPlayerInventoryDataModel("playersyncer_inventory", database);
        MySQLPlayerValuesDataModel valuesDataModel = new MySQLPlayerValuesDataModel("playersyner_values", database);

        enderChestDataModel.init();
        inventoryDataModel.init();
        valuesDataModel.init();

        this.modifier = initialize(new PlayerApplier());
        this.storage = new PlayerDataStorage(enderChestDataModel, valuesDataModel, inventoryDataModel);
        this.loop = new UpdateTimeoutLoop(Executors.newSingleThreadExecutor(), syncer, storage, modifier, conf.getExtendingTimeoutPeriod(), this.getLogger());
        this.loader = new PlayerLoader(storage, modifier, syncer, bukkitExecutor, new TimestampLogger(this.getLogger()), conf.getTimeout());

        conf.getStrategies().forEach(key -> this.modifier.setActivate(key, true));

        this.loop.start();

        this.listener = new PlayerJoinListener(loader);
        Bukkit.getPluginManager().registerEvents(listener, this);

        syncer.register(PlayerHolder.class, PlayerHolder::toString);

        this.bungeeJump.getStrategyRegistry().registerStrategy("sync-player", (future, context) -> {
            UUID uuid = context.getIssuer();
            this.loader.preTeleportSave(future, uuid);
        });
    }

    private PlayerApplier initialize(PlayerApplier modifier){
        modifier.registerStrategy("currentHealth", new SetCurrentHealth());
        modifier.registerStrategy("enderchest", new SetEnderChest());
        modifier.registerStrategy("equipments", new SetEquipments());
        modifier.registerStrategy("level", new SetLevel());
        modifier.registerStrategy("exp", new SetExp());
        modifier.registerStrategy("hungers", new SetHungers());
        modifier.registerStrategy("inventory", new SetInventory());
        return modifier;
    }
}
