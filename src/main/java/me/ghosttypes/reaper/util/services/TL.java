package me.ghosttypes.reaper.util.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TL { // Thread Loader
    public static ExecutorService cached = Executors.newCachedThreadPool();
    public static ScheduledExecutorService schedueled = Executors.newScheduledThreadPool(10);
    public static ExecutorService modules = Executors.newFixedThreadPool(10);

    public static void init() {
        // Thread pools initialized statically
    }

    public static void shutdown() {
        cached.shutdown();
        schedueled.shutdown();
        modules.shutdown();
    }
}
