package com.minepalm.syncer.player.bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class PlayerLoader {

    private static final long DEFAULT_SAVE_MILLS = 20000L;
    private static final long DEFAULT_LOAD_MILLS = 20000L;
    private static final long DEFAULT_LOAD_TIMEOUT_MILLS = 40000L;
    private static final long DEFAULT_SAVE_TIMEOUT_MILLS = 40000L;


    private final PlayerDataStorage storage;
    private final PlayerApplier modifier;
    private final SyncService service;
    private final BukkitExecutor executor;

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
        PlayerData data = null;
        try {
            hold(uuid);


            data = storage.getPlayerData(uuid).join();

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onLoad(uuid));
            }

            if(data == null){
                return LoadResult.FAILED;
            }

            if(data.getInventory() != null) {
                MySQLLogger.log(PlayerDataLog.loadLog(data));
            }

            val allTasks = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
            allTasks.get(DEFAULT_LOAD_TIMEOUT_MILLS, TimeUnit.MILLISECONDS);

            setCached(uuid, data);

            return LoadResult.SUCCESS;

        }catch (Throwable ex){
            MySQLLogger.report(data, ex, "load failed");
            return LoadResult.TIMEOUT;
        }

    }

    public boolean apply(Player player){
        UUID uuid = player.getUniqueId();
        boolean hasCached = hasCached(uuid);

        if(hasCached){
            PlayerData data = getCached(uuid);

            if(data != null) {
                PlayerData appliedData = modifier.inject(player, getCached(uuid));
                MySQLLogger.log(PlayerDataLog.apply(appliedData));
            }else{
                MySQLLogger.log(PlayerDataLog.applyNull(uuid));
            }

            for (LoadStrategy strategy : customLoadStrategies.values()) {
                try {
                    strategy.onApply(uuid);
                }catch (Throwable ex){
                    MySQLLogger.report(data, ex, "apply failed at strategy : "+strategy.getClass());
                }
            }
        }

        return hasCached;
    }

    public CompletableFuture<Boolean> saveAsync(UUID uuid, PlayerData data, String reason){
        return executor.async(()-> {
            if( System.currentTimeMillis() - checkPassed(uuid) >= 1000L ) {
                save(uuid, data, reason);
                return true;
            }else{
                return false;
            }
        });

    }

    public void hold(UUID uuid){
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        try{
            synced.hold(Duration.ofMillis(5000L + updatePeriodMills), userTimeoutMills);
        } catch (TimeoutException | InterruptedException | ExecutionException ignored) {

        }
    }

    public void release(UUID uuid) {
        try {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);
            synced.release();
        }catch (ExecutionException | InterruptedException  ignored){

        }
    }

    public void saveSync(UUID uuid, PlayerData data, String reason) {
        hold(uuid);
        try {
            save(uuid, data, reason);
        } finally {
            release(uuid);
        }
    }

    public void save(UUID uuid, PlayerData data, String description) {
        try {
            removeCached(uuid);
            if(modifier.isActivate("inventory")) {
                storage.save(uuid, data).get(10000L, TimeUnit.MILLISECONDS);
                MySQLLogger.log(PlayerDataLog.saveLog(data), description);
            }

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onUnload(uuid));
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(30000L, TimeUnit.MILLISECONDS);
            release(uuid);
            markPass(uuid);
        } catch (TimeoutException e) {
            MySQLLogger.report(data, e, "save timeout");
        }catch (ExecutionException | InterruptedException ex) {
            MySQLLogger.report(data, ex, "save failed");
        }
    }

    public void saveDisabled(UUID uuid, PlayerData data){
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);
        try {
            try {
                List<CompletableFuture<?>> list = new ArrayList<>();
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    list.add(strategy.onUnload(uuid));
                }

                if(modifier.isActivate("inventory")) {
                    storage.save(uuid, data).get(30000L, TimeUnit.MILLISECONDS);
                    MySQLLogger.log(PlayerDataLog.saveLog(data), "DISABLED");
                }
                CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(60000L, TimeUnit.MILLISECONDS);
            } finally {
                synced.release();
            }
        } catch (Throwable ex) {
            MySQLLogger.report(data, ex, "disable save failed");
        }
    }

    public void preTeleportLock(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            try {
                PlayerHolder holder = new PlayerHolder(uuid);
                Synced<PlayerHolder> synced = service.of(holder);
                if(!synced.isHold().join()){
                    synced.unsafe().hold();
                }
                save(uuid, modifier.extract(player), "teleport");
                markPass(uuid);
            }catch (Throwable ex){
                unmark(uuid);
                throw ex;
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
