package com.minepalm.syncer.player.bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class PlayerLoader {

    private static final long DEFAULT_SAVE_MILLS = 20000L;
    private static final long DEFAULT_LOAD_MILLS = 20000L;
    private static final long DEFAULT_LOAD_TIMEOUT_MILLS = 40000L;
    private static final long DEFAULT_SAVE_TIMEOUT_MILLS = 40000L;


    private final String currentServer;
    private final PlayerDataStorage storage;
    private final PlayerApplier modifier;
    private final BukkitExecutor executor;

    private final ConcurrentHashMap<String, LoadStrategy> customLoadStrategies = new ConcurrentHashMap<>();
    private final Cache<UUID, PlayerData> cachedData = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private final Set<UUID> successfullyLoaded = new HashSet<>();
    private final ConcurrentHashMap<UUID, Long> lastSavedTime = new ConcurrentHashMap<>();

    public void register(String key, LoadStrategy strategy){
        customLoadStrategies.put(key, strategy);
    }

    public LoadResult load(UUID uuid) throws ExecutionException, InterruptedException{
        PlayerData data = null;
        try {
            successfullyLoaded.remove(uuid);

            data = storage.getPlayerData(uuid).get(5000L, TimeUnit.MILLISECONDS);

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                strategy.onLoad(uuid).join();
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
            MySQLLogger.report(uuid, ex, "load failed");
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
            successfullyLoaded.add(player.getUniqueId());
        }

        return successfullyLoaded.contains(player.getUniqueId());
    }

    public CompletableFuture<Boolean> savePlayerQuit(UUID uuid, PlayerData data, String reason){
        /*
         * 이하 코드는 플레이어가 로그아웃할때 플레이어 데이터를 저장하는 경우로, 플레이어가 단순히 서버 이동이 아닌 로그아웃을 하는 경우에만
         * 실행되는 코드입니다.
         */
        long time = System.currentTimeMillis() - getLastSavedTime(uuid);
        if( time >= 5000L ) {
            return executor.async(() -> {
                save(uuid, data, reason);
                return true;
            });
        } else
            return CompletableFuture.completedFuture(false);

    }

    public void save(UUID uuid, PlayerData data, String description) {
        try {
            recordLastSave(uuid);
            removeCached(uuid);
            if(modifier.isActivate("inventory")) {
                storage.save(uuid, data).get(10000L, TimeUnit.MILLISECONDS);
                MySQLLogger.log(PlayerDataLog.saveLog(data), description);
            }

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                strategy.onUnload(uuid).join();
            }

            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(30000L, TimeUnit.MILLISECONDS);

            recordLastSave(uuid);
        } catch (TimeoutException ex) {
            MySQLLogger.report(data, ex, "save timeout");
        } catch (Throwable ex) {
            MySQLLogger.report(data, ex, "save failed");
        }
    }

    public void saveDisabled(UUID uuid, PlayerData data) {
        try {
            recordLastSave(uuid);
            removeCached(uuid);

            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onUnload(uuid));
            }

            if (modifier.isActivate("inventory")) {
                storage.save(uuid, data).get(30000L, TimeUnit.MILLISECONDS);
                MySQLLogger.log(PlayerDataLog.saveLog(data), "DISABLED");
            }
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get(60000L, TimeUnit.MILLISECONDS);

        } catch (Throwable ex) {
            MySQLLogger.report(data, ex, "disable save failed");
        }
    }

    public void preTeleportLock(UUID uuid, String dest) {
        Player player = Bukkit.getPlayer(uuid);

        if(player != null) {
            save(uuid, modifier.extract(player), currentServer+" -> "+dest);
        }
    }

    public void recordLastSave(UUID uuid){
        lastSavedTime.put(uuid, System.currentTimeMillis());
    }

    public long getLastSavedTime(UUID uuid){
        return lastSavedTime.getOrDefault(uuid, 0L);
    }

    public void invalidateUpdatedTime(UUID uuid){
        lastSavedTime.remove(uuid);
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

    public boolean isSuccessfullyLoaded(UUID uuid){
        return successfullyLoaded.contains(uuid);
    }

    public void invalidateLoaded(UUID uuid){
        successfullyLoaded.remove(uuid);
    }
}
