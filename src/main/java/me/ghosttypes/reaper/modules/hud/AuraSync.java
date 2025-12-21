package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.services.AuraSyncService;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * HUD element that syncs all RGB/rainbow elements in Reaper together.
 * When enabled, all HUD elements that support AuraSync will use the same rainbow color.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class AuraSync extends HudElement {
    public static final HudElementInfo<AuraSync> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "aura-sync",
        "Sync all RGB elements in Reaper together.",
        AuraSync::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    public final Setting<Boolean> syncHUDtext = sgGeneral.add(new BoolSetting.Builder()
        .name("hud-text")
        .description("Enable the HUD module and this setting to enable aura sync.")
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
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
        .name("text-color")
        .description("Color of the status text.")
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

    public AuraSync() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();

        if (isInEditor()) {
            // Display status in editor mode
            String statusText = isActive() ? "AuraSync - ON" : "AuraSync - OFF";
            setSize(renderer.textWidth(statusText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(statusText, x, y, textColor.get(), true, textScale);
            return;
        }

        // Update the global rainbow color each frame
        AuraSyncService.setSpeed(chromaSpeed.get());
        AuraSyncService.RGB_COLOR = AuraSyncService.getNext(renderer.delta);

        // Minimal rendering when not in editor (the element is essentially invisible)
        setSize(0, 0);
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
