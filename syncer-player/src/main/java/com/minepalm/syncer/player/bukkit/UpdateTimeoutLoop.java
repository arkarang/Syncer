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
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UpdateTimeoutLoop {

    private final ExecutorService service;
    private final Syncer syncer;

    private final PlayerDataStorage storage;

    private final PlayerApplier applier;
    private final long periodMills;

    private final long savePeriod;
    private final TimestampLogger logger;
    private final AtomicBoolean run = new AtomicBoolean(false);

    synchronized void start() {
        run.set(true);
        service.submit(() -> {
            while (run.get()) {
                try {
                    long estimateTime = loop();
                    if (periodMills >= estimateTime) {
                        Thread.sleep(periodMills - estimateTime);
                    }
                } catch (ExecutionException | InterruptedException e) {

                }
            }
        });
    }

    synchronized void end() {
        run.set(false);
    }

    private final ConcurrentHashMap<UUID, Long> lastUpdate = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, Long> lastSave = new ConcurrentHashMap<>();

    public void join(UUID uuid) {
        lastSave.put(uuid, System.currentTimeMillis());
        lastUpdate.put(uuid, System.currentTimeMillis());
    }

    public void quit(UUID uuid) {
        lastUpdate.remove(uuid);
        lastSave.remove(uuid);
    }

    long loop() throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();

        List<UUID> list = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<CompletableFuture<?>> saveFutures = new ArrayList<>();
        Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).forEach(list::add);

        for (UUID uuid : list) {
            PlayerHolder holder = new PlayerHolder(uuid);
            Synced<PlayerHolder> synced = syncer.of(holder);
            long lastSaveTime = lastSave.getOrDefault(uuid, 0L);
            long lastUpdateTime = lastUpdate.getOrDefault(uuid, 0L);

            if ( periodMills <= System.currentTimeMillis() - lastUpdateTime ) {

                lastUpdate.put(uuid, System.currentTimeMillis());
                val future = synced.updateTimeout(periodMills);

                if(savePeriod <= System.currentTimeMillis() - lastSaveTime) {
                    val future2 = future.thenAccept(completed -> {
                        if (completed) {
                            lastSave.put(uuid, System.currentTimeMillis());
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                PlayerData data = applier.extract(player);
                                saveFutures.add(storage.save(uuid, data).thenAccept(ignored -> {
                                    try {
                                        long value = lastUpdate.getOrDefault(uuid, 0L);
                                        if (value < System.currentTimeMillis()) {
                                            lastUpdate.put(uuid, System.currentTimeMillis());
                                        }
                                    } catch (Throwable e) {
                                        Bukkit.getLogger().severe(e.toString());
                                    }
                                }));
                                MySQLLogger.log(PlayerDataLog.saveLog(data), "AUTO");
                            }
                        }
                    });

                    futures.add(future2);
                }
            }
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(10000L, TimeUnit.MILLISECONDS);
            CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture<?>[0])).get(10000L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            int size1 = (int) saveFutures.stream().filter(CompletableFuture::isDone).count();
            Bukkit.getOnlinePlayers().stream().filter(Player::isOp).collect(Collectors.toList()).forEach(player -> {
                player.sendMessage("§4§l[PLAYER-SYNCER] 서버 자동저장 타임아웃 감지 ( 창고/채팅 확인해보세요 )");
            });
        }

        return System.currentTimeMillis() - now;

    }
}
