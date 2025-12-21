package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.Formatter;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * HUD element that displays a time-based greeting with the player's name.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Greeting extends HudElement {
    public static final HudElementInfo<Greeting> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "greeting",
        "Displays a time-based greeting.",
        Greeting::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

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

    public Greeting() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();
        String greetingText = getGreetingText();

        setSize(renderer.textWidth(greetingText, true, textScale), renderer.textHeight(true, textScale));
        renderer.text(greetingText, x, y, textColor.get(), true, textScale);
    }

    private String getGreetingText() {
        String greeting = Formatter.getGreeting();
        if (mc.player == null) return greeting;
        return greeting + mc.player.getName().getString();
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
