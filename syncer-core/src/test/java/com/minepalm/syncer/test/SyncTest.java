package com.minepalm.syncer.test;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.netty.HelloMain;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.DebugLogger;
import com.minepalm.syncer.core.Syncer;
import kr.msleague.mslibrary.database.MSLDatabases;
import kr.msleague.mslibrary.database.api.DatabaseConfig;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SyncTest {

    @Test
    @Ignore
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        MySQLDatabase database = new MySQLDatabase(Executors.newFixedThreadPool(4));
        DatabaseConfig config = MSLDatabases.HIKARI.copy();

        config.setAddress("localhost");
        config.setPort(3306);
        config.setDatabase("test");
        config.setUser("root");
        config.setPassword("test");

        database.connect(config);

        HelloEveryone network1 = new HelloMain("test1", new InetSocketAddress(25600), 12345, 67890, Logger.getLogger("global"));
        HelloEveryone network2 = new HelloMain("test2", new InetSocketAddress(25601), 12345, 67890, Logger.getLogger("global"));

        network1.getConnections().registerServerInfo("test2", new InetSocketAddress("127.0.0.1", 25601));
        network2.getConnections().registerServerInfo("test1", new InetSocketAddress("127.0.0.1", 25600));

        network1.getConnections().establishAll();
        network2.getConnections().establishAll();

        Syncer syncerA = new Syncer(database, network1);
        Syncer syncerB = new Syncer(database, network2);

        syncerA.initProcedures();

        TestToken token = new TestToken();

        syncerA.register(TestObject.class, token);
        syncerB.register(TestObject.class, token);

        TestObject obj = new TestObject("asdfasdf");

        Synced<TestObject> syncedA = syncerA.of(obj);
        Synced<TestObject> syncedB = syncerB.of(obj);

        ExecutorService executorA, executorB;
        executorA = Executors.newSingleThreadExecutor();
        executorB = Executors.newSingleThreadExecutor();

        Logger logger = LogManager.getLogManager().getLogger("global");
        DebugLogger.setLogger(logger);
        DebugLogger.setEnableDebug(true);

        long baseTime = System.currentTimeMillis();
        BaseTimer timer = new BaseTimer(baseTime);

        val taskA = runTest(executorA, syncedA, timer,1, 1000, "syncedA");
        val taskB = runTest(executorB, syncedB, timer,500, 1000, "syncedB");

        try {
            CompletableFuture.allOf(taskA, taskB).get(5000L, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            Assert.fail("task timeout");
        }

        Assert.assertTrue(timer.record() >= 2000);

    }

    CompletableFuture<Void> runTest(ExecutorService executor, Synced<TestObject> synced, BaseTimer timer, long runAfter, long holdTime, String label){
        return CompletableFuture.runAsync(()->{
            try{
                DebugLogger.log(label+" objectId: "+synced.getObjectKey());
                Thread.sleep(runAfter);
                DebugLogger.log(label+" start: "+timer.record());
                synced.hold(Duration.ofMillis(5000L));
                DebugLogger.log(label+" acquired hold: "+timer.record());
                Thread.sleep(holdTime);
                synced.release();
                DebugLogger.log(label+" release: "+timer.record());
            }catch (Throwable e){
                DebugLogger.error(e);
            }
        }, executor);
    }

    @RequiredArgsConstructor
    private static class BaseTimer{

        private final long baseTime;

        long record(){
            return System.currentTimeMillis() - baseTime;
        }
    }
}
