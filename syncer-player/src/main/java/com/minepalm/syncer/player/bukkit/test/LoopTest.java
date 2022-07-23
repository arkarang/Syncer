package com.minepalm.syncer.player.bukkit.test;

import com.minepalm.syncer.player.bukkit.LoadResult;
import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerLoader;
import com.minepalm.syncer.player.bukkit.PlayerSyncerConf;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoopTest {

    private final PlayerLoader loader;

    private final Logger logger;

    public void test1() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(()->{
            UUID arkarang = UUID.fromString("7ce52d57-8ac3-4daa-b9a1-5c3c0f8aadf4");
            for (int i = 0; i < 1000; i++) {
                try {
                    long currentTime = System.currentTimeMillis();
                    LoadResult result = loader.load(arkarang);

                    if (result == LoadResult.SUCCESS) {
                        PlayerData cached = loader.getCached(arkarang);

                        if (cached == null || cached.getInventory() == null) {
                            logger.info("failed test1 loaded inventory is null at trial " + i);
                            break;
                        }
                        logger.info("step1. load old inventory ("+cached.getInventory().getGeneratedTime()+") at "
                                +time(currentTime));

                        loader.save(arkarang, new PlayerData(cached.getUuid(), cached.getValues(),
                                cached.getInventory().copy(), cached.getEnderChest())).get();
                        logger.info("step2. save old inventory at " +time(currentTime));
                        loader.load(arkarang);

                        logger.info("step3. load new inventory at " +time(currentTime));

                        PlayerData data = loader.getCached(arkarang);
                        if (data == null || data.getInventory() == null) {
                            logger.info("failed test1 loaded after inventory is null at trial " + i);
                            break;
                        }

                        long gap = data.getInventory().getGeneratedTime() - cached.getInventory().getGeneratedTime();
                        logger.info("step4. load gap is " +gap);

                        if (currentTime > data.getInventory().getGeneratedTime()) {
                            logger.info("failed test1 : loaded data is not synced ("+gap+")at trial "+i);
                            break;
                        }
                    } else {
                        logger.info("failed test1 load result " + result.name() + " at trial " + i);
                        break;
                    }
                    logger.info("test passed trial "+i);
                } catch (Throwable e) {
                    logger.info("interrupted test cause " + e + " at " + i);
                }
            }
            logger.info("successfully passed");
        });
        executor.shutdown();
    }

    public void test2(){
        ExecutorService executor = Executors.newScheduledThreadPool(8);
        final UUID arkarang = UUID.fromString("7ce52d57-8ac3-4daa-b9a1-5c3c0f8aadf4");
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        List<CompletableFuture<?>> list = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            final int iz = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
                //logger.info("start test2 trial of " + iz);
                if(interrupted.get()){
                    //logger.info("cancelled test2 trial of " + iz);
                    return;
                }

                try {
                    long currentTime = System.currentTimeMillis();
                    LoadResult result = loader.load(arkarang);

                    if (result == LoadResult.SUCCESS) {
                        PlayerData cached = loader.getCached(arkarang);

                        if (cached == null || cached.getInventory() == null) {
                            logger.info("failed test1 loaded inventory is null at trial " + iz);
                            interrupted.set(true);
                        }

                        logger.info("step1. load old inventory ("+cached.getInventory().getGeneratedTime()+") at "
                                +time(currentTime));

                        loader.save(arkarang, new PlayerData(cached.getUuid(), cached.getValues(),
                                cached.getInventory().copy(), cached.getEnderChest())).get();
                        logger.info("step2. save old inventory at " +time(currentTime));
                        loader.load(arkarang);

                        logger.info("step3. load new inventory at " +time(currentTime));

                        PlayerData data = loader.getCached(arkarang);
                        if (data == null || data.getInventory() == null) {
                            logger.info("failed test1 loaded after inventory is null at trial " + iz);
                            interrupted.set(true);
                        }

                        long gap = data.getInventory().getGeneratedTime() - cached.getInventory().getGeneratedTime();
                        logger.info("step4. load gap is " +gap);

                        if (currentTime > data.getInventory().getGeneratedTime()) {
                            logger.info("failed test1 : loaded data is not synced ("+gap+")at trial "+iz);
                            interrupted.set(true);
                        }

                        logger.info("test2 passed trial "+iz);

                    } else {
                        logger.info("failed test2 load result " + result.name() + " at trial " + iz);
                        interrupted.set(true);
                    }

                }catch (Throwable e){
                    logger.info("interrupted test cause "+e+" at trial "+iz);
                    interrupted.set(true);
                }
            }, executor);
            list.add(future);
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenRun(()->{
            if(!interrupted.get()){
                logger.info("successfully passed test2");
            }
            executor.shutdown();
        });
    }

    private long time(long begin){
        return System.currentTimeMillis() - begin;
    }
}
