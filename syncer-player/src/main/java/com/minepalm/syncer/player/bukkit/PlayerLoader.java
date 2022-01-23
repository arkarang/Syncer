package com.minepalm.syncer.player.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.syncer.api.SyncService;
import com.minepalm.syncer.api.Synced;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
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
    private final long userTimeoutMills;
    private final ConcurrentHashMap<UUID, PlayerData> cachedData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> passed = new ConcurrentHashMap<>();

    public boolean load(UUID uuid) throws ExecutionException, InterruptedException{
        PlayerHolder holder = new PlayerHolder(uuid);
        Synced<PlayerHolder> synced = service.of(holder);

        cachedData.remove(uuid);

        try {
            synced.hold(Duration.ofMillis(50000L), userTimeoutMills);
            logger.log("uuid: "+uuid.toString()+" acquired player lock");
            PlayerData data = storage.getPlayerData(uuid).get();
            cachedData.put(uuid, data);
            logger.log("uuid: "+uuid.toString()+" pre login successful");
            return true;
        }catch (TimeoutException e){
            logger.log("uuid: "+uuid.toString()+"pre login timeout");
        }

        logger.log("uuid: "+uuid.toString()+" pre login failed");
        return false;

    }

    public boolean apply(Player player){
        UUID uuid = player.getUniqueId();

        if(cachedData.containsKey(uuid)){
            PlayerData data = cachedData.get(uuid);
            if(data != null) {
                modifier.inject(player, cachedData.get(uuid));
                logger.log(player, "successfully inject player data");
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

    public void save(Player player){
        UUID uuid = player.getUniqueId();

        if(passed.containsKey(uuid)){
            passed.remove(uuid);
            return;
        }

        executor.async(()->{
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = service.of(holder);

            try {
                try {
                    synced.hold(Duration.ofMillis(4000L), userTimeoutMills);
                    PlayerData data = modifier.extract(player);
                    storage.save(uuid, data);
                    logger.log(player, "save complete");
                } catch (TimeoutException e) {
                    logger.log(player, "timeout saving player inventory");
                } finally {
                    logger.log(player, "release player lock");
                    synced.release();
                }
            }catch (ExecutionException | InterruptedException ignored) {

            }

        });
    }

    public void preTeleportSave(CompletableFuture<?> future, UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            this.save(player);
            this.markPass(uuid);
            logger.log(player, "complete saving player data before teleport ");
            future.thenRun(()->{
                try {
                    service.of(new PlayerHolder(uuid)).release();
                    logger.log(player, "released player lock after teleport ");
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void markPass(UUID uuid){
        passed.put(uuid, true);
    }
}
