package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * HUD element that displays a customizable list of fake modules.
 * Useful for anti-screenshot purposes.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class ModuleSpoof extends HudElement {
    public static final HudElementInfo<ModuleSpoof> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "fake-modules",
        "Display a customizable list of fake modules.",
        ModuleSpoof::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode")
        .description("How to sort the fake modules list.")
        .defaultValue(SortMode.Shortest)
        .build()
    );

    private final Setting<List<String>> modules = sgGeneral.add(new StringListSetting.Builder()
        .name("modules")
        .description("List of fake module names to display.")
        .defaultValue(Collections.emptyList())
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor(255, 255, 255))
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

    public ModuleSpoof() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();
        sortModules();

        if (isInEditor() || modules.get().isEmpty()) {
            String displayText = "ModuleSpoof";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        // Calculate dimensions
        double width = 0;
        double height = 0;
        for (int i = 0; i < modules.get().size(); i++) {
            width = Math.max(width, renderer.textWidth(modules.get().get(i), true, textScale));
            height += renderer.textHeight(true, textScale);
            if (i > 0) height += 2;
        }
        setSize(width, height);

        // Render fake modules
        double currentY = y;
        for (int i = 0; i < modules.get().size(); i++) {
            String m = modules.get().get(i);
            renderer.text(m, x, currentY, textColor.get(), true, textScale);
            currentY += renderer.textHeight(true, textScale);
            if (i > 0) currentY += 2;
        }
    }

    private void sortModules() {
        switch (sortMode.get()) {
            case Shortest -> modules.get().sort(Comparator.comparing(String::length));
            case Longest -> modules.get().sort(Comparator.comparing(String::length).reversed());
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public enum SortMode {
        Longest,
        Shortest
    }
}
