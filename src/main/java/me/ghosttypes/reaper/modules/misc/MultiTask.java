package me.ghosttypes.reaper.modules.misc;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.events.InteractEvent;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import meteordevelopment.orbit.EventHandler;

public class MultiTask extends ReaperModule {
    public MultiTask() {
        super(Reaper.CATEGORY, "multi-task", "Allows you to eat while mining a block.");
    }

    @EventHandler
    public void onInteractEvent(InteractEvent event) {
        event.usingItem = false;
    }
}
