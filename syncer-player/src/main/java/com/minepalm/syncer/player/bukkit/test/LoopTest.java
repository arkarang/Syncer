package com.minepalm.syncer.player.bukkit.test;

import com.minepalm.syncer.player.bukkit.PlayerData;
import com.minepalm.syncer.player.bukkit.PlayerDataInventory;
import com.minepalm.syncer.player.bukkit.PlayerLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoopTest {

    private final PlayerLoader loader;

    private final Logger logger;

    /*
     * TODO:
     * 1. 들낙 거리는 것 테스트 해야함 ( 아주 빠르게, 시간 기준으로. )
     * 2. 로그아웃 하고 들어올때
     */
    public void test1(int seconds) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final UUID arkarang = UUID.fromString("7ce52d57-8ac3-4daa-b9a1-5c3c0f8aadf4");
        executor.submit(()->{
            long stop = System.currentTimeMillis() + seconds;
            int nextSlot = 0;
            int trial = 0;
            PlayerDataInventory before = null;
            while (stop > System.currentTimeMillis()){
                try {
                    trial++;
                    loader.load(arkarang);
                    PlayerData data = loader.getCached(arkarang);
                    PlayerDataInventory inventory = data.getInventory();
                    DataModifier modifier = new DataModifier(inventory);

                    if(before != null){
                        if(!DataModifier.assertEquals(before, inventory)){
                            Bukkit.getLogger().info("test1 failed trial "+trial+": inventory not equals");
                            return;
                        }
                    }else{
                        modifier.setItem(nextSlot, new ItemStack(Material.DIAMOND));
                    }


                    PlayerDataInventory moved;
                    if(nextSlot == 10){
                        moved = modifier.moveItem(10, 0);
                        nextSlot = 0;
                    }else {
                        moved = modifier.moveItem(nextSlot, nextSlot + 1);
                    }
                    PlayerData newPlayerData = new PlayerData(arkarang, data.getValues(), moved, data.getEnderChest());
                    before = moved;

                    loader.save(arkarang, newPlayerData, "TEST1");
                    nextSlot++;

                } catch (Throwable e) {
                    e.printStackTrace();
                    Bukkit.getLogger().warning("test1 failed trial "+trial+": error occurred "+e.getMessage());
                    return;
                }
            }

            Bukkit.getLogger().warning("test1 passed trial: "+trial);
        });
        //executor.shutdown();
    }

    private long time(long begin){
        return System.currentTimeMillis() - begin;
    }
}
