package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.services.AuraSyncService;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

import static me.ghosttypes.reaper.util.services.ResourceLoaderService.*;

/**
 * HUD element that displays the Reaper logo watermark.
 * Supports multiple logo designs and chroma animation.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Watermark extends HudElement {
    public static final HudElementInfo<Watermark> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "reaper-logo",
        "Displays the Reaper logo.",
        Watermark::new
    );

    public enum LogoDesign {
        Default,
        Beams,
        Colorsplash,
        Galaxy,
        PurpleGalaxy,
        RedGalaxy
    }

    private static final RainbowColor RAINBOW = new RainbowColor();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    public final Setting<LogoDesign> logo = sgGeneral.add(new EnumSetting.Builder<LogoDesign>()
        .name("logo")
        .description("Which logo to use.")
        .defaultValue(LogoDesign.Default)
        .build()
    );

    private final Setting<Double> boxW = sgGeneral.add(new DoubleSetting.Builder()
        .name("width")
        .description("Logo width.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 600)
        .build()
    );

    private final Setting<Double> boxH = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Logo height.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 600)
        .build()
    );

    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Chroma logo animation.")
        .defaultValue(false)
        .visible(() -> logo.get() == LogoDesign.Default)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Chroma animation speed.")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .visible(chroma::get)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("Logo tint color.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> !chroma.get())
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
        .defaultValue(2)
        .min(0.5)
        .sliderRange(0.5, 5)
        .build()
    );

    public Watermark() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        double logoScale = customScale.get() ? scale.get() : 1.0;
        double w = boxW.get() * logoScale;
        double h = boxH.get() * logoScale;
        setSize(w, h);

        Identifier logoId = switch (logo.get()) {
            case Default -> LOGO;
            case Beams -> LOGO_BEAMS;
            case Colorsplash -> LOGO_COLORSPLASH;
            case Galaxy -> LOGO_GALAXY;
            case PurpleGalaxy -> LOGO_PURPLE;
            case RedGalaxy -> LOGO_RED;
        };

        Color renderColor;
        if (chroma.get() && logo.get() == LogoDesign.Default) {
            RAINBOW.setSpeed(chromaSpeed.get() / 100);
            renderColor = AuraSyncService.isEnabled() ? AuraSyncService.RGB_COLOR : RAINBOW.getNext(renderer.delta);
        } else {
            renderColor = color.get();
        }

        renderer.texture(logoId, x, y, w, h, renderColor);
    }
}
