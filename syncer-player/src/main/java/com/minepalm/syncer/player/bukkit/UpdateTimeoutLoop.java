package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.player.MySQLLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class UpdateTimeoutLoop {

    private final ExecutorService service;
    private final Syncer syncer;

    private final PlayerDataStorage storage;

    private final PlayerApplier applier;
    private final long periodMills;
    private final TimestampLogger logger;
    private final AtomicBoolean run = new AtomicBoolean(false);

    synchronized void start(){
        run.set(true);
        service.submit(()->{
            while (run.get()){
                try {
                    long estimateTime = loop();
                    if(periodMills >= estimateTime){
                        Thread.sleep(periodMills - estimateTime);
                    }
                }catch (ExecutionException | InterruptedException e){

                }
            }
        });
    }

    synchronized void end(){
        run.set(false);
    }

    private volatile int loopCount = 0;

    synchronized long loop() throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        loopCount++;
        List<UUID> list = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<CompletableFuture<?>> saveFutures = new ArrayList<>();
        Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(list::add);

        for (UUID uuid : list) {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = syncer.of(holder);
            val future = synced.updateTimeout(periodMills);
            if(loopCount > 10) {
                val future2 = future.thenAccept(completed -> {
                    if (!completed) {
                        logger.warn("extending player lock timeout " + uuid + " is failed. is player offline?");
                    } else {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            try {
                                PlayerData data = applier.extract(player);
                                storage.save(uuid, data).get();
                                MySQLLogger.log(PlayerDataLog.saveLog(data), "AUTO");
                            } catch (InterruptedException | ExecutionException e) {
                                MySQLLogger.log(e);
                            }
                        }

                    }
                });
                futures.add(future2);
            }
        }

        if(loopCount > 10){
            loopCount = 0;
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(10000L, TimeUnit.MILLISECONDS);
            CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture<?>[0])).get(10000L, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            Bukkit.getLogger().warning("auto save loop timeout");
        }

        return System.currentTimeMillis() - now;
    }
}
