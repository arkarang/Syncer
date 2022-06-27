package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.bungeejump.impl.BungeeJump;
import com.minepalm.bungeejump.impl.bukkit.BungeeJumpBukkit;
import com.minepalm.syncer.bootstrap.SyncerBukkit;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.player.bukkit.strategies.*;
import com.minepalm.syncer.player.mysql.MySQLPlayerEnderChestDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerInventoryDataModel;
import com.minepalm.syncer.player.mysql.MySQLPlayerValuesDataModel;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import kr.travelrpg.travellibrary.bukkit.TravelLibraryBukkit;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerSyncer extends JavaPlugin {

    @Getter
    static PlayerSyncer inst;

    Syncer syncer;
    BungeeJump bungeeJump;
    UpdateTimeoutLoop loop;
    PlayerJoinListener listener;
    PlayerDataStorage storage;
    PlayerApplier modifier;
    @Getter
    PlayerLoader loader;

    @Override
    public void onEnable() {
        inst = this;
        PlayerSyncerConf conf = new PlayerSyncerConf(this, "config.yml");
        TimestampLogger logger = new TimestampLogger(this.getLogger());
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

        this.modifier = initialize(new PlayerApplier(logger));
        this.storage = new PlayerDataStorage(enderChestDataModel, valuesDataModel, inventoryDataModel);
        this.loop = new UpdateTimeoutLoop(Executors.newSingleThreadExecutor(), syncer, storage, modifier, conf.getExtendingTimeoutPeriod(), logger);
        this.loader = new PlayerLoader(storage, modifier, syncer, bukkitExecutor, logger, conf.getExtendingTimeoutPeriod(), conf.getTimeout());

        conf.getStrategies().forEach(key -> this.modifier.setActivate(key, true));

        logger.setLog(conf.logResults());
        this.loop.start();

        this.listener = new PlayerJoinListener(conf, loader);
        Bukkit.getPluginManager().registerEvents(listener, this);

        syncer.register(PlayerHolder.class, PlayerHolder::toString);

        this.bungeeJump.getStrategyRegistry().registerStrategy("sync-player", (context) -> {
            UUID uuid = context.getIssuer();
            this.loader.preTeleportSave(uuid);
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender){
            if(command.getName().equals("psalter")){
                //storage.getValuesDataModel().alter();
                storage.getInventoryDataModel().alter();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ExecutorService ex = Executors.newFixedThreadPool(4);

        for (Player player : Bukkit.getOnlinePlayers()) {
            val future  = CompletableFuture.supplyAsync(()->{
                PlayerData data = modifier.extract(player);
                return loader.saveDisabled(player.getUniqueId(), data);
            }, ex).thenCompose(subFuture -> subFuture);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private PlayerApplier initialize(PlayerApplier modifier){
        modifier.registerStrategy("enderchest", new SetEnderChest());
        modifier.registerStrategy("healthScale", new SetHealthScale());
        modifier.registerStrategy("inventory", new SetInventory());
        modifier.registerStrategy("equipments", new SetEquipments());
        modifier.registerStrategy("level", new SetLevel());
        modifier.registerStrategy("exp", new SetExp());
        modifier.registerStrategy("gamemode", new SetGamemode());
        modifier.registerStrategy("fly", new SetFly());
        modifier.registerStrategy("hungers", new SetHungers());
        modifier.registerStrategy("currentHealth", new SetCurrentHealth(this, Bukkit.getScheduler()));
        return modifier;
    }

}
