package com.minepalm.syncer.player.bukkit;

import co.aikar.commands.BukkitCommandManager;
import com.minepalm.arkarangutils.bukkit.ArkarangGUIListener;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.bungeejump.impl.BungeeJump;
import com.minepalm.bungeejump.impl.bukkit.BungeeJumpBukkit;
import com.minepalm.syncer.bootstrap.SyncerBukkit;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.bukkit.gui.PlayerDataGUIFactory;
import com.minepalm.syncer.player.bukkit.strategies.*;
import com.minepalm.syncer.player.bukkit.test.DuplicateFinder;
import com.minepalm.syncer.player.bukkit.test.LoopTest;
import com.minepalm.syncer.player.bukkit.test.TestCommand;
import com.minepalm.syncer.player.mysql.*;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import kr.travelrpg.travellibrary.bukkit.TravelLibraryBukkit;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    //TransactionLoop transactionLoop;

    //PlayerTransactionManager manager;

    @Override
    public void onEnable() {
        inst = this;
        PlayerSyncerConf conf = new PlayerSyncerConf(this, "config.yml");
        TimestampLogger logger = new TimestampLogger(this.getLogger());
        this.syncer = SyncerBukkit.inst();
        this.bungeeJump = BungeeJumpBukkit.getService();
        MySQLDatabase database = TravelLibraryBukkit.of().dataSource().mysql(conf.getMySQLName());
        MySQLDatabase logDatabase = TravelLibraryBukkit.of().dataSource().mysql(conf.getLogMySQLName());
        BukkitExecutor bukkitExecutor = new BukkitExecutor(this, Bukkit.getScheduler());

        MySQLPlayerEnderChestDataModel enderChestDataModel
                = new MySQLPlayerEnderChestDataModel("playersyncer_enderchest", database);
        MySQLPlayerInventoryDataModel inventoryDataModel
                = new MySQLPlayerInventoryDataModel("playersyncer_inventory", database);
        MySQLPlayerValuesDataModel valuesDataModel
                = new MySQLPlayerValuesDataModel("playersyner_values", database);

        enderChestDataModel.init();
        inventoryDataModel.init();
        valuesDataModel.init();

        this.modifier = initialize(new PlayerApplier(logger));
        //transactionLoop = new TransactionLoop(
        //        Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(4), 50);
        //transactionLoop.start();
        //manager = new PlayerTransactionManager(transactionLoop);
        //manager = new PlayerTransactionManager(Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool(), 50L);
        //manager.start();
        this.storage = new PlayerDataStorage(enderChestDataModel, valuesDataModel, inventoryDataModel);
        this.loop = new UpdateTimeoutLoop(Executors.newSingleThreadExecutor(), syncer, storage, modifier,
                conf.getExtendingTimeoutPeriod(), conf.getSavePeriod(), logger);
        this.loop.start();

        this.loader = new PlayerLoader(storage, modifier, syncer, bukkitExecutor,
                conf.getExtendingTimeoutPeriod(), conf.getTimeout());

        conf.getStrategies().forEach(key -> this.modifier.setActivate(key, true));

        logger.setLog(conf.logResults());

        this.listener = new PlayerJoinListener(conf, loader, modifier, bukkitExecutor, loop);
        Bukkit.getPluginManager().registerEvents(listener, this);

        syncer.register(PlayerHolder.class, PlayerHolder::toString);

        this.bungeeJump.getStrategyRegistry().registerStrategy("sync-player", (context) -> {
            UUID uuid = context.getIssuer();
            this.loader.preTeleportLock(uuid);
        });

        MySQLPlayerLogDatabase playerLogDatabase = new MySQLPlayerLogDatabase("playersyncer_logs", logDatabase);
        MySQLExceptionLogDatabase exceptionLogDatabase = new MySQLExceptionLogDatabase("playersyncer_excepitons", logDatabase);
        MySQLLogger.init(playerLogDatabase, exceptionLogDatabase);

        MySQLLogger.purge(System.currentTimeMillis() - 1000L *60*60*24*30*1);

        Bukkit.getScheduler().runTask(this, ()-> this.listener.setAllow());

        getCommand("pstest").setExecutor(new TestCommand(new LoopTest(loader, this.getLogger()), new DuplicateFinder(this.getLogger(), logDatabase)));
        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new InspectCommands(new BukkitExecutor(this, Bukkit.getScheduler()), playerLogDatabase, inventoryDataModel,
                new PlayerDataGUIFactory(inventoryDataModel)));
        ArkarangGUIListener.init();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //if(sender instanceof ConsoleCommandSender){
        //    if(command.getName().equals("psalter")){
        //        //storage.getValuesDataModel().alter();
        //        storage.getInventoryDataModel().alter();
        //        return true;
        //    }
        //}
        return false;
    }

    @Override
    @SneakyThrows
    public void onDisable() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : players) {
            PlayerData data = modifier.extract(player);
            loader.saveDisabled(player.getUniqueId(), data);
        }
        loop.end();
        getLogger().info("disable complete");

    }

    private PlayerApplier initialize(PlayerApplier modifier){
        modifier.registerStrategy("enderchest", new SetEnderChest());
        modifier.registerStrategy("healthScale", new SetHealthScale());
        modifier.registerStrategy("inventory", new SetInventory(this.getLogger()));
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
