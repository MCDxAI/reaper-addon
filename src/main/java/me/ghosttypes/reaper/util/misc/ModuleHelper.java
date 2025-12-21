package me.ghosttypes.reaper.util.misc;

import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Module management utilities
 * TODO: Full implementation when combat modules are ported
 */
public class ModuleHelper {

    public static void queueEZ(PlayerEntity target) {
        // TODO: Implement when AutoEZ module is ported
    }

    public static List<ReaperModule> combatModules = new ArrayList<>();

    public static void disableCombat() {
        combatModules.forEach(reaperModule -> {
            if (reaperModule.isActive()) reaperModule.toggle();
        });
    }

    public static void disableMovement() {
        // TODO: Implement when movement modules are ported
    }

    public static void disableCombat(Module parent) {
        for (Module m : combatModules) {
            if (m.equals(parent)) continue;
            if (m.isActive()) m.toggle();
        }
    }
}
