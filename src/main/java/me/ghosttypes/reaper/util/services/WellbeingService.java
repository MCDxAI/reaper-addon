package me.ghosttypes.reaper.util.services;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.Formatter;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wellness reminder service that sends periodic motivational messages to players
 */
public class WellbeingService {

    private static final String[] MESSAGES = {
        "Don't play for too long!",
        "Take a break and get some water!",
        "Get up and stretch!",
        "Don't spend all your time on block game!",
        "Hope you're having a good time!",
        "Tell people about us! " + Reaper.INVITE_LINK,
        "Remember to hydrate!",
        "looking cute ;)",
        "Leave us a review!"
    };

    public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void init() {
        executor.scheduleAtFixedRate(WellbeingService::alert, 0, 30, TimeUnit.MINUTES);
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void alert() {
        String msg = MESSAGES[Formatter.random(0, MESSAGES.length - 1)];
        ChatUtils.info(msg);
    }

}
