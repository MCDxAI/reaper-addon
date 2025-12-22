package me.ghosttypes.reaper.systems;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;

/**
 * Global configuration system for Reaper addon.
 * Stores notification settings and other global Reaper preferences.
 */
public class ReaperConfig extends System<ReaperConfig> {
    public final Settings settings = new Settings();

    private final SettingGroup sgNotifications = settings.createGroup("Notifications");

    // Notification Settings
    public final Setting<Boolean> info = sgNotifications.add(new BoolSetting.Builder()
        .name("info")
        .description("Show info messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> warning = sgNotifications.add(new BoolSetting.Builder()
        .name("warning")
        .description("Show warning messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> error = sgNotifications.add(new BoolSetting.Builder()
        .name("error")
        .description("Show error messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hide = sgNotifications.add(new BoolSetting.Builder()
        .name("hide")
        .description("Hide client-side messages.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> displayTime = sgNotifications.add(new IntSetting.Builder()
        .name("display-time")
        .description("How long each notification displays for (in seconds).")
        .defaultValue(2)
        .min(1)
        .sliderMax(10)
        .build()
    );

    public ReaperConfig() {
        super("reaper-config");
    }

    public static ReaperConfig get() {
        return Systems.get(ReaperConfig.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("settings", settings.toTag());
        return tag;
    }

    @Override
    public ReaperConfig fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompoundOrEmpty("settings"));
        return this;
    }
}
