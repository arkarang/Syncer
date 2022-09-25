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
    private final ConcurrentHashMap<UUID, Long> passed = new ConcurrentHashMap<>();

    public void register(String key, LoadStrategy strategy){
        customLoadStrategies.put(key, strategy);
    }

    public LoadResult load(UUID uuid) throws ExecutionException, InterruptedException{
        try {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            removeCached(uuid);

            synced.hold(Duration.ofMillis(5000L + updatePeriodMills), userTimeoutMills);

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
            return LoadResult.SUCCESS;

        }catch (Throwable ex){
            MySQLLogger.log(ex);
            return LoadResult.TIMEOUT;
        }

    }

    public boolean apply(Player player){
        UUID uuid = player.getUniqueId();

        if(hasCached(uuid)){
            PlayerData data = getCached(uuid);
            if(data != null) {
                PlayerData appliedData = modifier.inject(player, getCached(uuid));
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
            }
            removeCached(uuid);
            return true;
        }

        removeCached(uuid);
        return false;
    }

    public CompletableFuture<Void> saveRuntime(Player player, String reason){
        return executor.async(()-> {
            if( System.currentTimeMillis() - checkPassed(player.getUniqueId()) >= 100L) {
                UUID uuid = player.getUniqueId();
                save(uuid, modifier.extract(player), reason);
            }
        });

    }

    public void save(UUID uuid, PlayerData data, String reason) {
        removeCached(uuid);

        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        try {
            markPass(uuid);
            synced.hold(Duration.ofMillis(5000L + updatePeriodMills), userTimeoutMills);
            storage.save(uuid, data).get(3000L, TimeUnit.MILLISECONDS);

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onUnload(uuid));
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(3000L, TimeUnit.MILLISECONDS);
            synced.release();
            MySQLLogger.log(PlayerDataLog.saveLog(data), reason);
        } catch (TimeoutException e) {
            MySQLLogger.log(PlayerDataLog.saveTimeout(data));
        }catch (ExecutionException | InterruptedException ex) {
            MySQLLogger.log(ex);
        }finally {
            unmark(uuid);
        }
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

    public void preTeleportLock(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            try {
                save(uuid, modifier.extract(player), "teleport");
                markPass(uuid);
            }catch (Throwable ex){
                unmark(uuid);
                throw new IllegalStateException();
            }
        }
    }

    public void markPass(UUID uuid){
        passed.put(uuid, System.currentTimeMillis());
    }

    public long checkPassed(UUID uuid){
        return passed.getOrDefault(uuid, 0L);
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
