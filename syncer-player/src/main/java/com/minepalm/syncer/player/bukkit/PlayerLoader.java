package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.player.bukkit.strategies.LoadStrategy;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
    private final ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> passed = new ConcurrentHashMap<>();

    public void register(String key, LoadStrategy strategy){
        customLoadStrategies.put(key, strategy);
    }

    public LoadResult load(UUID uuid) throws ExecutionException, InterruptedException{
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        cachedData.remove(uuid);

        try {
            synced.hold(Duration.ofMillis(5000L+updatePeriodMills), userTimeoutMills);
            logger.log("uuid: "+uuid.toString()+" acquired player lock");
            PlayerData data = storage.getPlayerData(uuid).get();
            List<CompletableFuture<?>> list = new ArrayList<>();
            for (LoadStrategy strategy : customLoadStrategies.values()) {
                list.add(strategy.onLoad(uuid));
            }
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
            cachedData.put(uuid, data);
            logger.log("uuid: "+uuid.toString()+" pre login successful");
            return LoadResult.SUCCESS;
        }catch (TimeoutException e){
            logger.log("uuid: "+uuid.toString()+"pre login timeout");
            return LoadResult.TIMEOUT;
        }

    }

    public boolean apply(Player player){
        UUID uuid = player.getUniqueId();

        if(cachedData.containsKey(uuid)){
            PlayerData data = cachedData.get(uuid);
            if(data != null) {
                modifier.inject(player, cachedData.get(uuid));
                logger.log(player, "successfully inject player data");
                for (LoadStrategy strategy : customLoadStrategies.values()) {
                    strategy.onApply(uuid);
                }
            }else{
                logger.log(player, "skip null data");
            }
            logger.log(player, "complete player data load");
            cachedData.remove(uuid);
            return true;
        }

        logger.log(player, "failed player data load");
        return false;
    }

    public void saveRuntime(Player player){
        logger.log(player, " save start");
        UUID uuid = player.getUniqueId();

        if(passed.containsKey(uuid)){
            logger.log(player, " passed ");
            passed.remove(uuid);
        }else {
            executor.async(() -> {
                logger.log(player, " start async player save logic ");
                PlayerHolder holder = new PlayerHolder(uuid);
                Synced<PlayerHolder> synced = service.of(holder);

                try {
                    try {
                        synced.hold(Duration.ofMillis(4000L), userTimeoutMills);
                        PlayerData data = modifier.extract(player);
                        storage.save(uuid, data);
                        List<CompletableFuture<?>> list = new ArrayList<>();
                        for (LoadStrategy strategy : customLoadStrategies.values()) {
                            list.add(strategy.onUnload(uuid));
                        }
                        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
                        logger.log(player, "save complete");
                    } catch (TimeoutException e) {
                        logger.log(player, "timeout saving player inventory");
                    } finally {
                        logger.log(player, "release player lock");
                        synced.release();
                    }
                } catch (ExecutionException | InterruptedException ignored) {

                }

            });
        }
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
                CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
                return storage.save(uuid, data);
            } finally {
                synced.release();
            }
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            return CompletableFuture.completedFuture(null);
        }
    }

    public void preTeleportSave(UUID uuid){
        logger.log(uuid+" preTeleport save start");
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            this.saveRuntime(player);
            this.markPass(uuid);
            logger.log(player, "complete preTeleport request ");
        }
    }

    public void markPass(UUID uuid){
        passed.put(uuid, true);
    }

    public void unmark(UUID uuid){
        passed.remove(uuid);
    }
}
