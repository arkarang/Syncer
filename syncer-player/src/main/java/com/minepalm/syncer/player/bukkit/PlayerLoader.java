package com.minepalm.syncer.player.bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.player.MySQLLogger;
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

    private final ConcurrentHashMap<String, LoadStrategy> customLoadStrategies = new ConcurrentHashMap<>();
    private final Cache<UUID, PlayerData> cachedData = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();
    private final ConcurrentHashMap<UUID, String> passed = new ConcurrentHashMap<>();

    public void register(String key, LoadStrategy strategy){
        customLoadStrategies.put(key, strategy);
    }

    public LoadResult load(UUID uuid) throws ExecutionException, InterruptedException{
        try {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            removeCached(uuid);

            synced.hold(Duration.ofMillis(5000L + updatePeriodMills), userTimeoutMills);
            logger.log("uuid: " + uuid.toString() + " acquired player lock");

            PlayerData data = storage.getPlayerData(uuid).get(500L, TimeUnit.MILLISECONDS);

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onLoad(uuid));
            }

            if(data == null){
                return LoadResult.FAILED;
            }

            if(data.getInventory() == null){
                MySQLLogger.log(PlayerDataLog.nullLog(uuid, "LOAD_NULL"));
            }else{
                MySQLLogger.log(PlayerDataLog.loadLog(data));
            }

            setCached(uuid, data);
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
            logger.log("uuid: " + uuid.toString() + " pre login successful");
            return LoadResult.SUCCESS;

        }catch (Throwable ex){
            MySQLLogger.log(ex);
            logger.log("uuid: "+uuid.toString()+"pre login timeout");
            return LoadResult.TIMEOUT;
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
                    try {
                        strategy.onApply(uuid);
                    }catch (Throwable ex){
                        MySQLLogger.log(ex);
                    }
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

    public CompletableFuture<Void> saveRuntime(Player player, String reason){
        logger.log(player, " save start");
        UUID uuid = player.getUniqueId();
        removeCached(uuid);

        /*
        if(passed.containsKey(uuid)){
            logger.log(player, " passed ");
            MySQLLogger.log(PlayerDataLog.nullLog(uuid, "PASS"), passed.get(uuid));
            passed.remove(uuid);
            return CompletableFuture.completedFuture(null);
        }else {
            return save(uuid, modifier.extract(player), reason);
        }

         */
        return save(uuid, modifier.extract(player), reason);
    }

    public CompletableFuture<Void> save(UUID uuid, PlayerData data, String reason) {
        return executor.async(()->{
            logger.log(uuid + " start async player save logic ");
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            try {
                storage.save(uuid, data).get(3000L, TimeUnit.MILLISECONDS);
                List<CompletableFuture<?>> list = new ArrayList<>();
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    list.add(strategy.onUnload(uuid));
                }

                CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(3000L, TimeUnit.MILLISECONDS);
                MySQLLogger.log(PlayerDataLog.saveLog(data), reason);
                logger.log(uuid, "save complete");
            } catch (TimeoutException e) {
                logger.log(uuid, "timeout saving player inventory");
                MySQLLogger.log(PlayerDataLog.saveTimeout(data));
            }catch (ExecutionException | InterruptedException ex) {
                MySQLLogger.log(ex);
            } finally {
                logger.log(uuid, "release player lock");
                try {
                    synced.release();
                }catch (ExecutionException | InterruptedException ex) {
                    MySQLLogger.log(ex);
                }
            }

        });
    }

    public CompletableFuture<Void> saveDisabled(UUID uuid, PlayerData data){
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);
        try {
            try {
                List<CompletableFuture<?>> list = new ArrayList<>();
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    list.add(strategy.onUnload(uuid));
                }
                storage.save(uuid, data).get(3000L, TimeUnit.MILLISECONDS);
                MySQLLogger.log(PlayerDataLog.saveLog(data), "DISABLED");
                CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(3000L, TimeUnit.MILLISECONDS);
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
        //Bukkit.getLogger().warning("info: current thread: "+Thread.currentThread().getName());
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            try {
                this.saveRuntime(player, "PRE_TELEPORT").get(3000L, TimeUnit.MILLISECONDS);
                this.markPass(uuid, "TELEPORT");
                logger.log(player, "complete preTeleport request ");
            }catch (Throwable ex){
                MySQLLogger.log(ex);
            }
        }
    }

    public void markPass(UUID uuid, String reason){
        passed.put(uuid, reason);
    }

    public void unmark(UUID uuid){
        passed.remove(uuid);
    }

    public PlayerData getCached(UUID uuid){
        return cachedData.getIfPresent(uuid);
    }

    boolean hasCached(UUID uuid){
        return cachedData.getIfPresent(uuid) != null;
    }

    void setCached(UUID uuid, PlayerData data){
        cachedData.put(uuid, data);
    }

    void removeCached(UUID uuid){
        cachedData.invalidate(uuid);
    }
}
