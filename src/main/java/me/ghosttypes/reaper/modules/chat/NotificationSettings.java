package me.ghosttypes.reaper.modules.chat;

import me.ghosttypes.reaper.Reaper;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NotificationSettings extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> info = sgGeneral.add(new BoolSetting.Builder()
        .name("info")
        .description("Show info messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> warning = sgGeneral.add(new BoolSetting.Builder()
        .name("warning")
        .description("Show warning messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> error = sgGeneral.add(new BoolSetting.Builder()
        .name("error")
        .description("Show error messages as notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hide = sgGeneral.add(new BoolSetting.Builder()
        .name("hide")
        .description("Hide client-side messages.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> displayTime = sgGeneral.add(new IntSetting.Builder()
        .name("display-time")
        .description("How long each notification displays for (in seconds).")
        .defaultValue(2)
        .min(1)
        .build()
    );

    public NotificationSettings() {
        super(Reaper.CATEGORY, "notification-settings", "Settings for HUD notifications.");
    }
}
