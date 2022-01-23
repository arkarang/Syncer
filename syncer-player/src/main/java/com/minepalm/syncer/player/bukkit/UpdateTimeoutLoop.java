package com.minepalm.syncer.player.bukkit;

import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.Syncer;
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
import java.util.logging.Logger;

@RequiredArgsConstructor
public class UpdateTimeoutLoop {

    private final ExecutorService service;
    private final Syncer syncer;
    private final PlayerDataStorage storage;
    private final PlayerApplier applier;
    private final long periodMills;
    private final Logger logger;
    private final AtomicBoolean run = new AtomicBoolean(false);

    synchronized void start(){
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
                    logger.warning("extending player lock timeout "+uuid+" is failed. is player offline?");
                }
            });
            Optional.ofNullable(Bukkit.getPlayer(uuid)).ifPresent(player->{
                futures.add(storage.save(uuid, applier.extract(player)));
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();

        return System.currentTimeMillis() - now;
    }
}
