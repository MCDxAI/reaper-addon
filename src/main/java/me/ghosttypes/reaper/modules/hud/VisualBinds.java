package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.Sorter;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * HUD element that displays keybound modules and their binds.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class VisualBinds extends HudElement {
    public static final HudElementInfo<VisualBinds> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "visual-binds",
        "Display keybound modules and their bind.",
        VisualBinds::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Sorter.SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<Sorter.SortMode>()
        .name("sort-mode")
        .description("How to sort the binds list.")
        .defaultValue(Sorter.SortMode.Shortest)
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

    private final ArrayList<String> binds = new ArrayList<>();

    public VisualBinds() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();
        updateBinds();

        if (isInEditor()) {
            String displayText = "Keybinds";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        if (binds.isEmpty()) {
            String displayText = "You have no keybound modules.";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        // Calculate dimensions
        double width = 0;
        double height = 0;
        for (int i = 0; i < binds.size(); i++) {
            width = Math.max(width, renderer.textWidth(binds.get(i), true, textScale));
            height += renderer.textHeight(true, textScale);
            if (i > 0) height += 2;
        }
        setSize(width, height);

        // Render binds
        double currentY = y;
        for (int i = 0; i < binds.size(); i++) {
            String bind = binds.get(i);
            renderer.text(bind, x, currentY, textColor.get(), true, textScale);
            currentY += renderer.textHeight(true, textScale);
            if (i > 0) currentY += 2;
        }
    }

    private void updateBinds() {
        binds.clear();
        List<Module> modules = Modules.get().getAll().stream()
            .filter(module -> module.keybind.isSet())
            .toList();

        for (Module module : modules) {
            binds.add(module.title + ": [" + module.keybind.toString() + "]");
        }

        switch (sortMode.get()) {
            case Shortest -> binds.sort(Comparator.comparing(String::length));
            case Longest -> binds.sort(Comparator.comparing(String::length).reversed());
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
