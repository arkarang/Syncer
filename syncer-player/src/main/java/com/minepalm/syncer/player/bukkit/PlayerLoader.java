package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.PlayerTransactionManager;
import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class PlayerLoader {

    private final PlayerDataStorage storage;
    private final PlayerApplier modifier;
    private final SyncService service;
    private final BukkitExecutor executor;
    private final TimestampLogger logger;
    private final long updatePeriodMills;
    private final long userTimeoutMills;

    private final PlayerTransactionManager manager;

    private final ConcurrentHashMap<String, LoadStrategy> customLoadStrategies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> passed = new ConcurrentHashMap<>();

    public void register(String key, LoadStrategy strategy){
        customLoadStrategies.put(key, strategy);
    }

    public LoadResult load(UUID uuid) throws ExecutionException, InterruptedException{
        try {
            LocalLocks.lock(uuid);
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            removeCached(uuid);

            synced.hold(Duration.ofMillis(5000L + updatePeriodMills), userTimeoutMills);
            logger.log("uuid: " + uuid.toString() + " acquired player lock");

            PlayerData data = manager.commit(uuid, ()-> {
                try {
                    return storage.getPlayerData(uuid).get(3000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    MySQLLogger.log(ex);
                    return null;
                }
            }).get();

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onLoad(uuid));
            }

            if(data.getInventory() == null){
                MySQLLogger.log(PlayerDataLog.nullLog(uuid, "LOAD_NULL"));
            }else{
                MySQLLogger.log(PlayerDataLog.loadLog(data));
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
            setCached(uuid, data);
            logger.log("uuid: " + uuid.toString() + " pre login successful");
            return LoadResult.SUCCESS;

        }catch (Throwable ex){
            MySQLLogger.log(ex);
            logger.log("uuid: "+uuid.toString()+"pre login timeout");
            return LoadResult.TIMEOUT;
        }finally {
            LocalLocks.unlock(uuid);
        }

    }

    public boolean apply(Player player){
        UUID uuid = player.getUniqueId();

        if(hasCached(uuid)){
            PlayerData data = getCached(uuid);
            if(data != null) {
                PlayerData appliedData = modifier.inject(player, getCached(uuid));
                logger.log(player, "successfully inject player data");
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    strategy.onApply(uuid);
                }
                MySQLLogger.log(PlayerDataLog.apply(appliedData));
            }else{
                MySQLLogger.log(PlayerDataLog.applyNull(uuid));
                logger.log(player, "skip null data");
            }
            logger.log(player, "complete player data load");
            removeCached(uuid);
            return true;
        }

        removeCached(uuid);
        logger.log(player, "failed player data load");
        return false;
    }

    public void saveRuntime(Player player){
        logger.log(player, " save start");
        UUID uuid = player.getUniqueId();

        if(passed.containsKey(uuid)){
            logger.log(player, " passed ");
            MySQLLogger.log(PlayerDataLog.nullLog(uuid, "PASS"), passed.get(uuid));
            passed.remove(uuid);
        }else {
            save(uuid, modifier.extract(player));
        }

    }

    public CompletableFuture<Void> save(UUID uuid, PlayerData data) {
        return manager.commit(uuid, ()->{
            LocalLocks.lock(uuid);
            logger.log(uuid + " start async player save logic ");
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            try {
                try {
                    synced.hold(Duration.ofMillis(4000L), userTimeoutMills);
                    storage.save(uuid, data).get();
                    List<CompletableFuture<?>> list = new ArrayList<>();
                    for (LoadStrategy strategy : customLoadStrategies.values()) {
                        list.add(strategy.onUnload(uuid));
                    }
                    CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
                    MySQLLogger.log(PlayerDataLog.saveLog(data));
                    logger.log(uuid, "save complete");
                } catch (TimeoutException e) {
                    logger.log(uuid, "timeout saving player inventory");
                    MySQLLogger.log(PlayerDataLog.saveTimeout(data));
                } finally {
                    logger.log(uuid, "release player lock");
                    synced.release();
                }
            } catch (ExecutionException | InterruptedException ex) {
                MySQLLogger.log(ex);
            }finally {
                LocalLocks.unlock(uuid);
            }
        });
    }

    public CompletableFuture<Void> saveDisabled(UUID uuid, PlayerData data){
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);
        try {
            try {
                synced.hold(Duration.ofMillis(4000L), userTimeoutMills);
                List<CompletableFuture<?>> list = new ArrayList<>();
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    list.add(strategy.onUnload(uuid));
                }
                MySQLLogger.log(PlayerDataLog.saveLog(data), "DISABLED");
                CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
                return storage.save(uuid, data);
            } finally {
                synced.release();
            }
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            MySQLLogger.log(ex);
            return CompletableFuture.completedFuture(null);
        }
    }

    public void preTeleportSave(UUID uuid){
        logger.log(uuid+" preTeleport save start");
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            this.saveRuntime(player);
            this.markPass(uuid, "TELEPORT");
            logger.log(player, "complete preTeleport request ");
        }
    }

    public void markPass(UUID uuid, String reason){
        passed.put(uuid, reason);
    }

    public void unmark(UUID uuid){
        passed.remove(uuid);
    }

    public synchronized PlayerData getCached(UUID uuid){
        return cachedData.get(uuid);
    }

    synchronized boolean hasCached(UUID uuid){
        return cachedData.containsKey(uuid);
    }

    synchronized void setCached(UUID uuid, PlayerData data){
        cachedData.put(uuid, data);
    }

    synchronized void removeCached(UUID uuid){
        cachedData.remove(uuid);
    }
}
