package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.Syncer;
import com.minepalm.syncer.player.MySQLLogger;
import com.minepalm.syncer.player.PlayerTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class UpdateTimeoutLoop {

    private final ExecutorService service;
    private final Syncer syncer;

    private final PlayerTransactionManager manager;
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

    synchronized long loop() throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        List<UUID> list = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(list::add);

        for (UUID uuid : list) {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = syncer.of(holder);
            val future = synced.updateTimeout(periodMills);
            future.thenAccept(completed -> {
                if(!completed){
                    logger.warn("extending player lock timeout "+uuid+" is failed. is player offline?");
                }else{
                    Optional.ofNullable(Bukkit.getPlayer(uuid)).ifPresent(player->{
                        val future2 = manager.commit(uuid, ()-> {
                            try {
                                PlayerData data = applier.extract(player);
                                storage.save(uuid, data).get();
                                MySQLLogger.log(PlayerDataLog.saveLog(data), "AUTO");
                            } catch (InterruptedException | ExecutionException e) {
                                MySQLLogger.log(e);
                            }
                            return null;
                        });
                        futures.add(future2);
                        future2.thenRun(()->{
                            logger.log(player, "auto save completed");
                        });
                    });
                    futures.add(future);
                }
            });
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();

        return System.currentTimeMillis() - now;
    }
}
