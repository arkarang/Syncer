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
    PlayerJoinListener listener;
    PlayerDataStorage storage;
    PlayerApplier modifier;
    @Getter
    PlayerLoader loader;
    @Override
    public void onEnable() {
        inst = this;
        PlayerSyncerConf conf = new PlayerSyncerConf(this, "config.yml");
        this.syncer = SyncerBukkit.inst();
        String current = syncer.getHolderRegistry().getLocalName();
        this.bungeeJump = BungeeJumpBukkit.getService();
        MySQLDatabase database = TravelLibraryBukkit.of().dataSource().mysql(conf.getMySQLName());
        MySQLDatabase logDatabase = TravelLibraryBukkit.of().dataSource().mysql(conf.getLogMySQLName());
        BukkitExecutor bukkitExecutor = new BukkitExecutor(this, Bukkit.getScheduler());

        this.modifier = initialize(new PlayerApplier());
        this.storage = new PlayerDataStorage(database);

        this.loader = new PlayerLoader(current, storage, modifier, bukkitExecutor);

        conf.getStrategies().forEach(key -> this.modifier.setActivate(key, true));

        this.listener = new PlayerJoinListener(current, conf, loader, modifier, bukkitExecutor);
        Bukkit.getPluginManager().registerEvents(listener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerFreezer(), this);

        syncer.register(PlayerHolder.class, PlayerHolder::toString);

        this.bungeeJump.getStrategyRegistry().registerStrategy("sync-player", (context) -> {
            UUID uuid = context.getIssuer();
            this.loader.preTeleportLock(uuid, context.getServer());
        });

        MySQLPlayerLogDatabase playerLogDatabase = new MySQLPlayerLogDatabase("playersyncer_logs", logDatabase);
        MySQLErrorReportDatabase exceptionLogDatabase = new MySQLErrorReportDatabase("playersyncer_error_reports", logDatabase);
        MySQLLogger.init(current, playerLogDatabase, exceptionLogDatabase);

        MySQLLogger.purge(System.currentTimeMillis() - 1000L *60*60*24*30*1);

        Bukkit.getScheduler().runTask(this, ()-> this.listener.setAllow());

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new InspectCommands(new BukkitExecutor(this, Bukkit.getScheduler()), playerLogDatabase,
                storage.getInventoryDataModel(), storage.getEnderChestDataModel(),
                new PlayerDataGUIFactory(storage.getInventoryDataModel(), storage.getEnderChestDataModel())));
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
        modifier.registerStrategy("potion", new SetPotion());
        return modifier;
    }

}
