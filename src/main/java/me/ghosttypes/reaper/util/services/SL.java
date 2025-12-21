package me.ghosttypes.reaper.util.services;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.MathUtil;
import me.ghosttypes.reaper.util.misc.MessageUtil;
import me.ghosttypes.reaper.util.os.OSUtil;
import meteordevelopment.meteorclient.MeteorClient;

/**
 * Service Loader - Initializes all background services
 */
public class SL {

    public static void load() {
        long start = MathUtil.now();
        OSUtil.init(); // setup current OS
        ResourceLoaderService.init(); // download assets
        MeteorClient.EVENT_BUS.subscribe(new GlobalManager()); // subscribe GlobalManager to events
        MessageUtil.init();
        NotificationManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(TL::shutdown));
        Reaper.log("Started services (" + MathUtil.msPassed(start) + "ms)");
    }

    // TODO: Add RPC setup when RPC module is ported
    // public static void setupRPC() { ... }
}
