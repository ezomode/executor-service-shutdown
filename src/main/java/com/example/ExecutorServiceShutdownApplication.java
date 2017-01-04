package com.example;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class ExecutorServiceShutdownApplication {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("main() from thread=" + Thread.currentThread().getName());

        SomeClass someClass = new SomeClass();

        // Pick one.
//        new Thread(someClass::doStuffAndScheduleShutdown).start();
//        new Thread(someClass::shutdownThroughFuture).start();
//        new Thread(someClass::shutdownThroughFutureAsync).start();
        new Thread(someClass::shutdownThroughRunnable).start();
    }
}

class SomeClass {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("named-thread-%d")
            .build();
    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1, threadFactory);
    private final ScheduledExecutorService singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
    private Stopwatch stopwatch;
    private int counter;

    private DateFormat df = new SimpleDateFormat("HH:mm:ss");

    public SomeClass() {
        createStopwatch();
    }

    void doStuffAndScheduleShutdown() {

        final Runnable runnable = () -> {
            System.out.println("do stuff in thread: " + Thread.currentThread().getName());
            counter++;

            if (counter >= 4) {
                shutdownScheduler(null, "throwing an NPE from doStuffAndScheduleShutdown()");
            }
        };

        scheduledThreadPool.scheduleAtFixedRate(runnable, 100, 1000, MILLISECONDS);

        scheduledThreadPool.schedule(() -> {
            System.out.println("Shutting down from thread: " + Thread.currentThread().getName() + " at millis: " + getTime());
            scheduledThreadPool.shutdown();
        }, 9000, MILLISECONDS);

        System.out.println("End of doStuffAndScheduleShutdown() at millis: " + getTime() + "\n");
    }

    void shutdownThroughFuture() {

        // Runnable will throw an NPE.
        ScheduledFuture<?> future = singleThreadScheduledExecutor.scheduleWithFixedDelay(getRunnable(null), 100, 1000, MILLISECONDS);

        try {
            future.get();
        } catch (Exception e) {
            System.out.println("Caught exception: " + e.getMessage());
            shutdownScheduler(singleThreadScheduledExecutor, "shutting down singleThreadScheduledExecutor");
            //            e.printStackTrace();
        }

        System.out.println("End of shutdownThroughFuture()\n");
    }

    void shutdownThroughFutureAsync() {

        // Runnable will throw an NPE.
        ScheduledFuture<?> future = singleThreadScheduledExecutor.scheduleWithFixedDelay(getRunnable(null), 100, 1000, MILLISECONDS);

        scheduledThreadPool.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Before future.get() in shutdownThroughFutureAsync()");

                future.get();

                System.out.println("After future.get() in shutdownThroughFutureAsync()");
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage());
                shutdownScheduler(singleThreadScheduledExecutor, "shutting down singleThreadScheduledExecutor");
                shutdownScheduler(scheduledThreadPool, "shutting down scheduledThreadPool");
                //                e.printStackTrace();
            }
        }, 500, 500, MILLISECONDS);


        System.out.println("End of shutdownThroughFutureAsync()\n");
    }

    private Stopwatch createStopwatch() {
        stopwatch = Stopwatch.createStarted();

        return stopwatch;
    }

    void shutdownThroughRunnable() {

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(threadFactory);

        executorService.scheduleWithFixedDelay(getRunnable(executorService), 100, 1000, MILLISECONDS);

        System.out.println("End of shutdownThroughRunnable()\n");
    }

    private Runnable getRunnable(ExecutorService executorService) {
        return () -> {

            System.out.println("do stuff in thread=" + Thread.currentThread().getName());
            counter++;

            if (counter >= 4) {
                shutdownScheduler(executorService, "counter>=4"); // instead of throw...
                //            throw new RuntimeException("!!!");
            }
        };
    }

    private void shutdownScheduler(ExecutorService executorService, String description) {
        executorService.shutdown();

        System.out.println("Shutting down due to EXCEPTION thread(" + Thread.currentThread().getName() + ") description: " + description + " at millis: " + getTime());
    }

    private String getTime() {
        return String.valueOf(stopwatch.elapsed(MILLISECONDS));
    }
}