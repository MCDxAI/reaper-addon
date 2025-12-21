package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.world.BlockHelper;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.Comparator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * HUD element for debugging Reaper functionality.
 * Displays various player state information.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class DebugHud extends HudElement {
    public static final HudElementInfo<DebugHud> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "debug-hud",
        "Reaper debug HUD.",
        DebugHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Boolean> solidFoot = sgGeneral.add(new BoolSetting.Builder()
        .name("solid-feet")
        .description("Display if block at feet is solid.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> solidAboveFoot = sgGeneral.add(new BoolSetting.Builder()
        .name("solid-above-feet")
        .description("Display if block above feet is solid.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> inSwimmingPose = sgGeneral.add(new BoolSetting.Builder()
        .name("in-swimming-pose")
        .description("Display if player is in swimming pose.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> health = sgGeneral.add(new BoolSetting.Builder()
        .name("health")
        .description("Display total health.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    // Scale settings
    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this HUD element.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    public DebugHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();

        if (!Utils.canUpdate()) {
            String displayText = "debug-hud";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        ArrayList<String> stats = getDebugInfo();

        if (isInEditor() || stats.isEmpty()) {
            String displayText = "Debug";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        // Calculate dimensions
        double width = 0;
        double height = 0;
        for (int i = 0; i < stats.size(); i++) {
            width = Math.max(width, renderer.textWidth(stats.get(i), true, textScale));
            height += renderer.textHeight(true, textScale);
            if (i > 0) height += 2;
        }
        setSize(width, height);

        // Render debug info
        double currentY = y;
        for (int i = 0; i < stats.size(); i++) {
            String s = stats.get(i);
            renderer.text(s, x, currentY, textColor.get(), true, textScale);
            currentY += renderer.textHeight(true, textScale);
            if (i > 0) currentY += 2;
        }
    }

    private ArrayList<String> getDebugInfo() {
        ArrayList<String> stats = new ArrayList<>();

        if (mc.player == null) return stats;

        if (solidFoot.get()) {
            boolean solid = !BlockHelper.isAir(mc.player.getBlockPos());
            stats.add("Solid Feet: " + solid);
        }

        if (solidAboveFoot.get()) {
            boolean solid = !BlockHelper.isAir(mc.player.getBlockPos().up());
            stats.add("Solid Above Feet: " + solid);
        }

        if (inSwimmingPose.get()) {
            stats.add("In Swimming Pose: " + mc.player.isInSwimmingPose());
        }

        if (health.get()) {
            stats.add("Health: " + String.format("%.1f", PlayerUtils.getTotalHealth()));
        }

        stats.sort(Comparator.comparing(String::length));
        return stats;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
