package me.ghosttypes.reaper.modules.misc;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import me.ghosttypes.reaper.util.player.Interactions;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.EXPThrower;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;

/**
 * AntiAim module - Makes your head spin around to confuse opponents.
 * Features anti-desync options to stop spinning during certain actions.
 */
public class AntiAim extends ReaperModule {
    private final SettingGroup sgDefault = settings.getDefaultGroup();

    private final Setting<Mode> antiDesync = sgDefault.add(new EnumSetting.Builder<Mode>()
        .name("anti-desync")
        .description("Stops spinning on some triggers.")
        .defaultValue(Mode.All)
        .build());

    private final Setting<Boolean> yaw = sgDefault.add(new BoolSetting.Builder()
        .name("yaw")
        .description("Spin around horizontally.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> ySpeed = sgDefault.add(new IntSetting.Builder()
        .name("yaw-speed")
        .description("The speed at which you rotate horizontally.")
        .defaultValue(5)
        .range(1, 100)
        .sliderRange(1, 100)
        .visible(yaw::get)
        .build());

    private final Setting<Boolean> pitch = sgDefault.add(new BoolSetting.Builder()
        .name("pitch")
        .description("Spin around vertically.")
        .defaultValue(false)
        .build());

    private final Setting<Integer> pSpeed = sgDefault.add(new IntSetting.Builder()
        .name("pitch-speed")
        .description("The speed at which you rotate vertically.")
        .defaultValue(5)
        .range(1, 100)
        .sliderRange(1, 100)
        .visible(pitch::get)
        .build());

    private short count = 0;
    private short yCount = 0;
    private short pCount = 0;

    public AntiAim() {
        super(Reaper.CATEGORY, "anti-aim", "Makes your head spin around to confuse opponents.");
    }

    @Override
    public void onActivate() {
        count = 0;
        yCount = 0;
        pCount = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Check if we should stop spinning
        if (antiDesync.get() != Mode.None && (shouldStop() || (antiDesync.get() == Mode.All && Interactions.isInElytra()))) {
            return;
        }

        // Update yaw rotation
        yCount += ySpeed.get();
        if (yCount > 180) yCount = -180;

        // Update pitch rotation
        if (pitch.get()) {
            count++;
            if (count <= pSpeed.get()) pCount = 90;
            if (count > pSpeed.get()) pCount = -90;
            if (count >= pSpeed.get() + pSpeed.get()) count = 0;
        }

        // Apply rotation
        Rotations.rotate(
            yaw.get() ? yCount : mc.player.getYaw(),
            pitch.get() ? pCount : mc.player.getPitch()
        );
    }

    private boolean shouldStop() {
        if (mc.player == null) return true;

        // Stop if EXPThrower or BedAura is active
        if (Modules.get().isActive(EXPThrower.class) ||
            Modules.get().isActive(meteordevelopment.meteorclient.systems.modules.combat.BedAura.class)) {
            return true;
        }

        // Stop if holding certain items
        return mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem ||
            mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem ||
            mc.player.getMainHandStack().getItem() instanceof EnderPearlItem ||
            mc.player.getOffHandStack().getItem() instanceof EnderPearlItem ||
            mc.player.getMainHandStack().getItem() instanceof BowItem ||
            mc.player.getOffHandStack().getItem() instanceof BowItem;
    }

    public enum Mode {
        All,
        ExceptElytra,
        None
    }
}
