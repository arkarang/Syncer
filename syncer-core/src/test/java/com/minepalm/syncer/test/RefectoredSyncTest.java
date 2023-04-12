package com.minepalm.syncer.test;

import com.minepalm.library.database.DatabaseConfig;
import com.minepalm.library.database.impl.internal.DefaultHikariConfig;
import com.minepalm.library.database.impl.internal.MySQLDB;
import com.minepalm.library.network.api.PalmNetwork;
import com.minepalm.library.network.server.HelloMain;
import com.minepalm.syncer.api.Synced;
import com.minepalm.syncer.core.DebugLogger;
import com.minepalm.syncer.core.Syncer;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RefectoredSyncTest {
    private static final int NETWORK_PORT_1 = 25600;
    private static final int NETWORK_PORT_2 = 25601;
    private static final int TEST_DURATION_MS = 5000;
    private static final int TASK_TIMEOUT_MS = 5000;

    private Syncer syncerA;
    private Syncer syncerB;

    @BeforeEach
    public void setUp() {
        var mysql = new MySQLDB(Executors.newFixedThreadPool(4));
        DatabaseConfig config = new DefaultHikariConfig();

        config.set("useSSL", "false");
        config.setAddress("localhost");
        config.setPort("3306");
        config.setDatabase("test");
        config.setUser("root");
        config.setPassword("test");

        mysql.connect(config);

        var database = mysql.java();

        PalmNetwork network1 = new HelloMain("test1", new InetSocketAddress(NETWORK_PORT_1), 12345, 67890, Logger.getLogger("global"));
        PalmNetwork network2 = new HelloMain("test2", new InetSocketAddress(NETWORK_PORT_2), 12345, 67890, Logger.getLogger("global"));

        network1.getConnections().registerServerInfo("test2", new InetSocketAddress("127.0.0.1", NETWORK_PORT_2));
        network2.getConnections().registerServerInfo("test1", new InetSocketAddress("127.0.0.1", NETWORK_PORT_1));

        network1.getConnections().establishAll();
        network2.getConnections().establishAll();

        syncerA = new Syncer(database, network1);
        syncerB = new Syncer(database, network2);
    }

    @Test
    @DisplayName("Syncer objects should be able to hold and release objects with correct synchronization")
    public void testSyncerHoldAndRelease() {
        syncerA.initProcedures();

        TestToken token = new TestToken();

        syncerA.register(TestObject.class, token);
        syncerB.register(TestObject.class, token);

        TestObject obj = new TestObject("asdfasdf");

        Synced<TestObject> syncedA = syncerA.of(obj);
        Synced<TestObject> syncedB = syncerB.of(obj);

        ScheduledExecutorService executorA = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService executorB = Executors.newSingleThreadScheduledExecutor();

        Logger logger = LogManager.getLogManager().getLogger("global");
        DebugLogger.setLogger(logger);
        DebugLogger.setEnableDebug(true);

        long baseTime = System.currentTimeMillis();
        BaseTimer timer = new BaseTimer(baseTime);

        CompletableFuture<Void> taskA = runTest(executorA, syncedA, timer, 1, TEST_DURATION_MS, "syncedA");
        CompletableFuture<Void> taskB = runTest(executorB, syncedB, timer, 500, TEST_DURATION_MS, "syncedB");

        try {
            CompletableFuture.anyOf(taskA, taskB).get(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Assert.fail("Task failed with exception: " + e.getMessage());
        }

        Assert.assertTrue(timer.record() >= TEST_DURATION_MS);
    }

    private CompletableFuture<Void> runTest(ScheduledExecutorService executor, Synced<TestObject> synced, BaseTimer timer, long runAfter, long holdTime, String label){
        CompletableFuture<Void> future = new CompletableFuture<>();
        executor.schedule(() -> {
                    try {
                        DebugLogger.log(label + " objectId: " + synced.getObjectKey());
                        DebugLogger.log(label + " start: " + timer.record());
                        synced.hold(Duration.ofMillis(TEST_DURATION_MS));
                        DebugLogger.log(label + " acquired hold: " + timer.record());
                        synced.hold(Duration.ofMillis(TEST_DURATION_MS));
                        DebugLogger.log(label + " acquired hold: " + timer.record());

                        try {
                            Thread.sleep(holdTime);
                            synced.release();
                            DebugLogger.log(label + " release: " + timer.record());
                            future.complete(null);
                        } catch (Throwable e) {
                            DebugLogger.error(e);
                            future.completeExceptionally(e);
                        }
                    } catch (Throwable e) {
                        DebugLogger.error(e);
                        future.completeExceptionally(e);
                    }
                }, runAfter, TimeUnit.MILLISECONDS);


        return future;
    }
    @RequiredArgsConstructor
    private static class BaseTimer {
        private final long baseTime;

        long record() {
            return System.currentTimeMillis() - baseTime;
        }
    }
}
