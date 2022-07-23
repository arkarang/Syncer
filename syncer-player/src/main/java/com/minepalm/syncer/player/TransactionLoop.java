package com.minepalm.syncer.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TransactionLoop {
    private final ExecutorService loop;
    private final ExecutorService workers;
    private final long periodMills;
    AtomicBoolean run = new AtomicBoolean(false);
    private CompletableFuture<Void> mainLoopTask;
    private final List<CompletableFuture<Void>> runningTasks = new ArrayList<>();

    ConcurrentHashMap<UUID, PlayerTransaction> transactions = new ConcurrentHashMap<>();

    public TransactionLoop(ExecutorService loop, ExecutorService workers, long periodMills){
        this.loop = loop;
        this.workers = workers;
        this.periodMills = periodMills;
    }

    public synchronized void start(){
        this.run.set(true);
        mainLoopTask = CompletableFuture.runAsync(()->{
            while (run.get()){
                runningTasks.clear();
                long begin = System.currentTimeMillis();

                List<PlayerTransaction> list = new ArrayList<>(this.transactions.values());

                for (PlayerTransaction transaction : list) {
                    runningTasks.add(CompletableFuture.runAsync(transaction::runIfHasNext, workers));
                }

                try {
                    CompletableFuture.allOf(runningTasks.toArray(new CompletableFuture<?>[0])).get();
                }catch (Throwable e){
                    MySQLLogger.log(e);
                }
                long estimatedTime = System.currentTimeMillis() - begin;
                if(estimatedTime < periodMills){
                    try {
                        Thread.sleep(periodMills - estimatedTime);
                    } catch (InterruptedException ignored) {

                    }
                }
            }
        }, loop);
    }

    public synchronized CompletableFuture<Void> stop(){
        run.set(false);
        transactions.values().stream().map(PlayerTransaction::shutdown).collect(Collectors.toList());
        workers.shutdown();
        loop.shutdown();
        return mainLoopTask;
    }

    public synchronized void register(UUID uuid, PlayerTransaction controller){
        this.transactions.put(uuid, controller);
    }

    public synchronized void unregister(UUID uuid){
        this.transactions.remove(uuid);
    }
}
