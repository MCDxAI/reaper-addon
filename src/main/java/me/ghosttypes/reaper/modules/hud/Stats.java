package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.player.Interactions;
import me.ghosttypes.reaper.util.services.AuraSyncService;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * HUD element that displays various client and combat statistics.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Stats extends HudElement {
    public static final HudElementInfo<Stats> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "reaper-stats",
        "Displays various client info.",
        Stats::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCombat = settings.createGroup("Combat");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    // General settings
    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Rainbow color effect.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> chromaText = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma-text")
        .description("Makes the text rainbow too.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Rainbow animation speed.")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .visible(chroma::get)
        .build()
    );

    public final Setting<Boolean> drawBack = sgGeneral.add(new BoolSetting.Builder()
        .name("render-background")
        .description("Render a background behind the stats.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> drawSide = sgGeneral.add(new BoolSetting.Builder()
        .name("render-side")
        .description("Render colored side accent.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color.")
        .defaultValue(new SettingColor(50, 50, 50, 150))
        .build()
    );

    public final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Side accent color.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    public final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode")
        .description("How to sort the stats list.")
        .defaultValue(SortMode.Shortest)
        .build()
    );

    private final Setting<Boolean> fps = sgGeneral.add(new BoolSetting.Builder()
        .name("fps")
        .description("Display FPS.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> tps = sgGeneral.add(new BoolSetting.Builder()
        .name("tps")
        .description("Display TPS.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ping = sgGeneral.add(new BoolSetting.Builder()
        .name("ping")
        .description("Display ping.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> playtime = sgGeneral.add(new BoolSetting.Builder()
        .name("playtime")
        .description("Display playtime.")
        .defaultValue(false)
        .build()
    );

    // Combat settings
    private final Setting<Boolean> deaths = sgCombat.add(new BoolSetting.Builder()
        .name("deaths")
        .description("Display your total deaths.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> highscore = sgCombat.add(new BoolSetting.Builder()
        .name("highscore")
        .description("Display your highest killstreak.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> kd = sgCombat.add(new BoolSetting.Builder()
        .name("kd-ratio")
        .description("Display your kills to death ratio.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> kills = sgCombat.add(new BoolSetting.Builder()
        .name("kills")
        .description("Display your total kills.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> killstreak = sgCombat.add(new BoolSetting.Builder()
        .name("killstreak")
        .description("Display your current killstreak.")
        .defaultValue(false)
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

    private static final RainbowColor RAINBOW = new RainbowColor();

    public Stats() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();
        ArrayList<String> stats = getStats();

        if (isInEditor() || stats.isEmpty()) {
            String displayText = "Stats";
            double width = renderer.textWidth(displayText, true, textScale);
            double height = renderer.textHeight(true, textScale);
            setSize(width, height);
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

        // Setup colors
        RAINBOW.setSpeed(chromaSpeed.get() / 100);
        Color chromaColor = RAINBOW.getNext(renderer.delta);
        if (AuraSyncService.isEnabled()) chromaColor = AuraSyncService.RGB_COLOR;

        Color currentSideColor = chroma.get() ? chromaColor : sideColor.get();
        Color currentTextColor = chromaText.get() ? chromaColor : textColor.get();

        // Render stats
        double currentY = y;
        for (int i = 0; i < stats.size(); i++) {
            String s = stats.get(i);
            double textWidth = renderer.textWidth(s, true, textScale);
            double lineHeight = renderer.textHeight(true, textScale);

            // Draw background
            if (drawBack.get()) {
                renderer.quad(x - 2, currentY - 2, textWidth + 4, lineHeight + 2, backColor.get());
            }

            // Draw side accent
            if (drawSide.get()) {
                renderer.quad(x - 4, currentY - 2, 2, lineHeight + 2, currentSideColor);
            }

            // Draw text
            renderer.text(s, x, currentY, currentTextColor, true, textScale);

            currentY += lineHeight;
            if (i > 0) currentY += 2;
        }
    }

    private ArrayList<String> getStats() {
        ArrayList<String> stats = new ArrayList<>();

        if (!Utils.canUpdate()) return stats;

        // General stats
        if (fps.get()) stats.add("FPS: " + MinecraftClientAccessor.meteor$getFps());
        if (tps.get()) stats.add("TPS: " + String.format("%.1f", TickRate.INSTANCE.getTickRate()));
        if (ping.get()) stats.add("Ping: " + Interactions.getCurrentPing());
        if (playtime.get()) stats.add("Playtime: " + me.ghosttypes.reaper.util.player.Stats.getPlayTime());

        // Combat stats
        if (deaths.get()) stats.add("Deaths: " + Interactions.getDeaths());
        if (highscore.get()) stats.add("Highscore: " + Interactions.getHighscore());
        if (kd.get()) stats.add("KD: " + Interactions.getKD());
        if (kills.get()) stats.add("Kills: " + Interactions.getKills());
        if (killstreak.get()) stats.add("Killstreak: " + Interactions.getKillstreak());

        // Sort
        switch (sortMode.get()) {
            case Shortest -> stats.sort(Comparator.comparing(String::length));
            case Longest -> stats.sort(Comparator.comparing(String::length).reversed());
        }

        return stats;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public enum SortMode {
        Longest,
        Shortest
    }
}
