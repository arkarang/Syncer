package com.minepalm.syncer.player.test;

public class LoopTest {

    /*
    final Map<UUID, List<Integer>> asserted = new HashMap<>();
    final Map<UUID, List<Integer>> actual = new HashMap<>();

    @Test
    public void test2() throws ExecutionException, InterruptedException, TimeoutException {
        PlayerTransactionManager loop = new PlayerTransactionManager(Executors.newSingleThreadExecutor(),
                Executors.newCachedThreadPool(), 50L);

        Map<UUID, Boolean> map = new HashMap<>();
        loop.start();

        for (int i = 0; i < 100; i++) {
            UUID uuid = UUID.randomUUID();
            map.put(uuid, true);
            generateTask(uuid, loop);
        }
        try {
            loop.shutdown().get(10000L, TimeUnit.MILLISECONDS);
            //System.out.println("task all completed");
        } catch (TimeoutException e) {
            //System.out.println("left tasks: "+loop.getTasks().size());
        }
        //System.out.println("assertion ---- ");
        int count = 0;
        for (UUID uuid : map.keySet()) {
            count++;
            List<Integer> actualList = actual.get(uuid);
            List<Integer> assertedList = asserted.get(uuid);
            Assert.assertEquals(actualList.size(), assertedList.size());
            //System.out.println("let's check count of " + count);
            //System.out.println("expected values: "+assertedList);
            //System.out.println("actual values: "+actualList);
            for (int i = 0; i < actualList.size(); i++) {
                int actualNumber = actualList.get(i);
                int assertNumber = assertedList.get(i);
                Assert.assertEquals(assertNumber, actualNumber);
            }
        }

    }

    public void oldTransactionManagerTest() throws ExecutionException, InterruptedException, TimeoutException {
        OldTransactionManager loop = new OldTransactionManager(Executors.newSingleThreadExecutor());
        Map<UUID, Boolean> map = new HashMap<>();
        //loop.start();
        for(int i = 0 ; i < 100 ; i++){
            UUID uuid = UUID.randomUUID();
            map.put(uuid, true);
            generateTask(uuid, loop);
        }
        Thread.sleep(5000L);

        try {
            loop.shutdown().get(10000L, TimeUnit.MILLISECONDS);
            //System.out.println("task all completed");
        }catch (TimeoutException e){
            //System.out.println("left tasks: "+loop.getTasks().size());
        }

        //System.out.println("assertion ---- ");
        int count = 0;
        for (UUID uuid : map.keySet()) {
            count++;
            List<Integer> actualList = actual.get(uuid);
            List<Integer> assertedList = asserted.get(uuid);
            System.out.println("let's check count of " + count);
            System.out.println("expected values: "+assertedList);
            System.out.println("actual values: "+actualList);
            Assert.assertEquals(actualList.size(), assertedList.size());
            for (int i = 0; i < actualList.size(); i++) {
                int actualNumber = actualList.get(i);
                int assertNumber = assertedList.get(i);
                Assert.assertEquals(assertNumber, actualNumber);
            }
        }
    }

    AtomicInteger total = new AtomicInteger(0);

    public void generateTask(final UUID uuid, final PlayerTransactionManager loop){
        actual.put(uuid, new ArrayList<>());
        asserted.put(uuid, new ArrayList<>());
        int random = new Random().nextInt(100);

        for(int i = 0 ; i < random ; i++){
            final int num = new Random().nextInt(100);
            asserted.get(uuid).add(num);
        }

        asserted.get(uuid).forEach( num2 -> {
            loop.commit(uuid, ()->{

                actual.get(uuid).add(num2);
                int size = asserted.get(uuid).size() - 1;
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    public void generateTask(final UUID uuid, final OldTransactionManager loop){
        actual.put(uuid, new ArrayList<>());
        asserted.put(uuid, new ArrayList<>());
        int random = new Random().nextInt(10);

        for(int i = 0 ; i < random ; i++){
            final int num = new Random().nextInt(10);
            asserted.get(uuid).add(num);
        }

        asserted.get(uuid).forEach( num2 -> {
            loop.commit(uuid, ()->{

                actual.get(uuid).add(num2);
                int size = asserted.get(uuid).size() - 1;
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }
    */

}
