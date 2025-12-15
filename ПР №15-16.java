import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {



    static List<Integer> runWithExecutor(ExecutorService executor, int tasks) throws Exception {
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < tasks; i++) {
            final int value = i;
            futures.add(executor.submit(() -> {
                Thread.sleep(5);
                return value * value;
            }));
        }

        List<Integer> results = new ArrayList<>();
        for (Future<Integer> f : futures) {
            results.add(f.get());
        }
        return results;
    }



    static void concurrentCollectionsDemo() throws Exception {

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 10; i++) {
            final int idx = i;
            pool.submit(() -> {
                map.put("k" + idx, idx);
                list.add("v" + idx);
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("\n=== CONCURRENT COLLECTIONS ===");
        System.out.println("ConcurrentHashMap size = " + map.size());
        System.out.println("CopyOnWriteArrayList size = " + list.size());


    }



    static void completableFutureDemo() throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(4);

        CompletableFuture<Integer> f1 =
                CompletableFuture.supplyAsync(() -> slowCalc(10), pool);

        CompletableFuture<Integer> f2 =
                CompletableFuture.supplyAsync(() -> slowCalc(20), pool);

        CompletableFuture<Integer> combined =
                f1.thenCombine(f2, Integer::sum);

        CompletableFuture<Void> all =
                CompletableFuture.allOf(f1, f2);

        all.join(); // очікуємо завершення

        System.out.println("\n=== COMPLETABLE FUTURE ===");
        System.out.println("f1 = " + f1.get());
        System.out.println("f2 = " + f2.get());
        System.out.println("combined = " + combined.get());

        pool.shutdown();
    }

    static int slowCalc(int x) {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        return x * 2;
    }



    static void timeoutAndExceptionDemo() {

        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> {
                    sleep(300);
                    return 42;
                });

        CompletableFuture<Integer> safe =
                future
                        .orTimeout(200, TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> {
                            System.out.println("Timeout або помилка: " + ex);
                            return -1; // альтернативний шлях
                        });

        System.out.println("\n=== TIMEOUT / EXCEPTION ===");
        System.out.println("Result = " + safe.join());
    }


    static void scalabilityTest() throws Exception {

        int[] taskCounts = {10, 100, 500, 1000};

        System.out.println("\n=== SCALABILITY TEST ===");

        for (int tasks : taskCounts) {

            ExecutorService pool =
                    Executors.newFixedThreadPool(
                            Runtime.getRuntime().availableProcessors());

            long start = System.nanoTime();
            runWithExecutor(pool, tasks);
            long end = System.nanoTime();

            pool.shutdown();

            System.out.printf(
                    "Tasks: %-5d Time: %d ms%n",
                    tasks,
                    (end - start) / 1_000_000
            );
        }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }


    public static void main(String[] args) throws Exception {

        System.out.println("=== EXECUTOR SERVICE ===");

        ExecutorService fixedPool = Executors.newFixedThreadPool(4);
        ExecutorService workStealingPool = Executors.newWorkStealingPool();

        List<Integer> fixedResult =
                runWithExecutor(fixedPool, 20);

        List<Integer> wsResult =
                runWithExecutor(workStealingPool, 20);

        fixedPool.shutdown();
        workStealingPool.shutdown();

        System.out.println("Fixed pool result size = " + fixedResult.size());
        System.out.println("Work-stealing result size = " + wsResult.size());

        concurrentCollectionsDemo();
        completableFutureDemo();
        timeoutAndExceptionDemo();
        scalabilityTest();

        conclusions();
    }

    static void conclusions() {
        System.out.println
    }
}
