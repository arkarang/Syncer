package com.minepalm.syncer.player;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerTransactionManager {

    /*
    private final ExecutorService main;
    private final ExecutorService worker;

    private final long period;

    private final AtomicBoolean run = new AtomicBoolean(false);

    private final Map<UUID, ConcurrentLinkedDeque<Runnable>> map = new ConcurrentHashMap<>();
    private final Map<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicBoolean> status = new ConcurrentHashMap<>();

    private final AtomicInteger taskIdGenerator = new AtomicInteger(0);
    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();

    @RequiredArgsConstructor
    private static class Task{
        final int id;
        final CompletableFuture<?> task;

    }

    public void start(){
        run.set(true);
        main.execute(this::loop);
    }

    public void loop(){
        while (run.get()){
            long begin = System.currentTimeMillis();

            synchronized (this) {
                List<Runnable> currentTasks = new ArrayList<>();

                for (UUID uuid : map.keySet()) {
                    if(!status.get(uuid).get()) {
                        Runnable task = map.get(uuid).poll();
                        if (task != null)
                            currentTasks.add(task);
                    }
                }

                List<CompletableFuture<?>> futures = new ArrayList<>();
                for (Runnable currentTask : currentTasks) {
                    CompletableFuture<?> future = CompletableFuture.runAsync(currentTask, worker);
                    futures.add(future);
                }

                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(7500L, TimeUnit.MILLISECONDS);
                } catch (Throwable e) {
                    MySQLLogger.log(e);
                }

            }

            long estimatedTime = System.currentTimeMillis() - begin;

            if(estimatedTime < period){
                try {
                    Thread.sleep(period - estimatedTime);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    public synchronized CompletableFuture<Void> commit(UUID uuid, Runnable run){
        init(uuid);
        CompletableFuture<Void> callee = new CompletableFuture<>();

        Task task = new Task(taskIdGenerator.addAndGet(1), callee);
        tasks.put(task.id, task);

        map.get(uuid).add(()->{
            try {
                synchronized (locks.get(uuid)) {
                    status.get(uuid).set(true);
                    run.run();
                    callee.complete(null);
                }
            } catch (Throwable ex) {
                callee.completeExceptionally(ex);
                MySQLLogger.log(ex);
            }finally {
                tasks.remove(task.id);
                status.get(uuid).set(false);
            }

        });
        return callee;
    }

    public synchronized <T> CompletableFuture<T> commit(UUID uuid, Supplier<T> sup) {
        init(uuid);

        CompletableFuture<T> callee = new CompletableFuture<>();
        Task task = new Task(taskIdGenerator.addAndGet(1), callee);
        tasks.put(task.id, task);

        map.get(uuid).add(() -> {
            try {
                synchronized (locks.get(uuid)) {
                    status.get(uuid).set(true);
                    T result = sup.get();
                    callee.complete(result);
                }
            } catch (Throwable ex) {
                callee.completeExceptionally(ex);
                MySQLLogger.log(ex);
            } finally {
                tasks.remove(task.id);
                status.get(uuid).set(false);

            }
        });

        return callee;
    }

    public synchronized <T> CompletableFuture<T> commit(UUID uuid, CompletableFuture<T> future){
        init(uuid);

        CompletableFuture<T> callee = new CompletableFuture<>();
        Task task = new Task(taskIdGenerator.addAndGet(1), callee);
        tasks.put(task.id, task);

        map.get(uuid).add(()->{
            try {
                synchronized (locks.get(uuid)) {
                    status.get(uuid).set(true);;
                    T result = future.get(5000L, TimeUnit.MILLISECONDS);
                    callee.complete(result);
                }
            } catch (Throwable ex) {
                callee.completeExceptionally(ex);
            }finally {
                tasks.remove(task.id, task);
                status.get(uuid).set(false);
            }

        });

        return callee;
    }

    private synchronized void init(UUID uuid){
        status.putIfAbsent(uuid, new AtomicBoolean(false));
        map.putIfAbsent(uuid, new ConcurrentLinkedDeque<>());
        locks.putIfAbsent(uuid, new ReentrantLock());
    }

    public CompletableFuture<Void> shutdown() throws ExecutionException, InterruptedException, TimeoutException {
        return CompletableFuture.allOf(tasks.values().stream().map(task -> task.task).toArray(CompletableFuture[]::new)).thenAccept(ignored->run.set(false));
    }

    public synchronized Collection<Task> getTasks(){
        return tasks.values();
    }

     */


}
