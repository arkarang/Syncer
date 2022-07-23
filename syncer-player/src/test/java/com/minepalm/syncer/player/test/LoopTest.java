package com.minepalm.syncer.player.test;

import com.minepalm.syncer.player.PlayerTransaction;
import com.minepalm.syncer.player.TransactionLoop;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoopTest {

    final Map<UUID, List<Integer>> asserted = new HashMap<>();
    final Map<UUID, List<Integer>> actual = new HashMap<>();

    @Test
    public void test(){
        TransactionLoop loop = new TransactionLoop(Executors.newSingleThreadExecutor(),
                Executors.newFixedThreadPool(4), 50L);
        loop.start();
        Map<UUID, PlayerTransaction> map = new HashMap<>();
        for(int i = 0 ; i < 100 ; i++){
            UUID uuid = UUID.randomUUID();
            PlayerTransaction transaction = new PlayerTransaction(uuid);
            map.put(uuid, transaction);
            loop.register(uuid, transaction);
            generateTask(uuid, transaction);
        }
        loop.stop();
        System.out.println("assertion ---- ");
        int count = 0;
        for (UUID uuid : map.keySet()) {
            count++;
            List<Integer> actualList = actual.get(uuid);
            List<Integer> assertedList = asserted.get(uuid);
            Assert.assertEquals(actualList.size(), assertedList.size());
            System.out.println("let's check count of "+count);
            for(int i = 0 ; i < actualList.size() ; i++){
                int actualNumber = actualList.get(i);
                int assertNumber = assertedList.get(i);
                Assert.assertEquals(assertNumber, actualNumber);
            }
        }
    }

    AtomicInteger total = new AtomicInteger(0);

    public void generateTask(UUID uuid, PlayerTransaction transaction){
        actual.put(uuid, new ArrayList<>());
        asserted.put(uuid, new ArrayList<>());
        int random = new Random().nextInt(10);
        for(int i = 0 ; i < random ; i++){
            final int num = new Random().nextInt(100);
            asserted.get(uuid).add(num);
        }


        asserted.get(uuid).forEach( num2 -> {
            transaction.addTask(()->{
                try {
                    actual.get(uuid).add(num2);
                    int size = asserted.get(uuid).size();
                    System.out.println("execute task ["+actual.get(uuid).size()+"/"+size+"] "+total.addAndGet(1));
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

}
