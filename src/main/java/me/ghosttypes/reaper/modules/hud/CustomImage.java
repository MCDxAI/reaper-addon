package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.MathUtil;
import me.ghosttypes.reaper.util.services.AuraSyncService;
import me.ghosttypes.reaper.util.services.ResourceLoaderService;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Identifier;

/**
 * HUD element that displays a custom image from file or URL.
 * Supports chroma animation and periodic refresh from URL.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class CustomImage extends HudElement {
    public static final HudElementInfo<CustomImage> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "custom-image",
        "Displays a custom image",
        CustomImage::new
    );

    public enum LogoMode {File, URL}

    private final Identifier IMAGE = Identifier.of("reaper", "custom_png");
    private static final RainbowColor RAINBOW = new RainbowColor();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<LogoMode> logoMode = sgGeneral.add(new EnumSetting.Builder<LogoMode>()
        .name("logo")
        .description("Which logo source to use.")
        .defaultValue(LogoMode.File)
        .onChanged(mode -> setTexture())
        .build()
    );

    private final Setting<String> fileName = sgGeneral.add(new StringSetting.Builder()
        .name("file-name")
        .description("The file to load the texture from")
        .defaultValue("cope.png")
        .visible(() -> logoMode.get() == LogoMode.File)
        .onChanged(name -> setTexture())
        .build()
    );

    private final Setting<String> url = sgGeneral.add(new StringSetting.Builder()
        .name("url")
        .description("The URL to load the texture from")
        .defaultValue("cope.com")
        .visible(() -> logoMode.get() == LogoMode.URL)
        .onChanged(urlStr -> setTexture())
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Image scale multiplier.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .onChanged(s -> calculateSize())
        .build()
    );

    private final Setting<Double> boxW = sgGeneral.add(new DoubleSetting.Builder()
        .name("box-width")
        .description("Base image width.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 600)
        .onChanged(w -> calculateSize())
        .build()
    );

    private final Setting<Double> boxH = sgGeneral.add(new DoubleSetting.Builder()
        .name("box-height")
        .description("Base image height.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 600)
        .onChanged(h -> calculateSize())
        .build()
    );

    public final Setting<Boolean> update = sgGeneral.add(new BoolSetting.Builder()
        .name("refresh")
        .description("Reload the image after a set period of time")
        .defaultValue(false)
        .visible(() -> logoMode.get() == LogoMode.URL)
        .build()
    );

    public final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("refresh-delay")
        .description("Refresh delay in seconds.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .visible(update::get)
        .build()
    );

    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Chroma logo animation.")
        .defaultValue(false)
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
        .name("background-color")
        .description("Image tint color.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> !chroma.get())
        .build()
    );

    private long lastRefresh = MathUtil.now();

    public CustomImage() {
        super(INFO);
        calculateSize();
    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        setTexture(); // Load texture when player first joins the game
    }

    public void calculateSize() {
        setSize(boxW.get() * scale.get(), boxH.get() * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        double w = getWidth();
        double h = getHeight();

        // Auto-refresh from URL if enabled
        if (update.get() && logoMode.get() == LogoMode.URL && MathUtil.msPassed(lastRefresh) >= updateDelay.get() * 1000) {
            lastRefresh = MathUtil.now();
            setTexture();
        }

        // Setup color
        Color renderColor;
        if (chroma.get()) {
            RAINBOW.setSpeed(chromaSpeed.get() / 100);
            renderColor = AuraSyncService.isEnabled() ? AuraSyncService.RGB_COLOR : RAINBOW.getNext(renderer.delta);
        } else {
            renderColor = color.get();
        }

        // Render texture using 1.21.11 API
        renderer.texture(IMAGE, x, y, w, h, renderColor);
    }

    private void setTexture() {
        switch (logoMode.get()) {
            case File -> ResourceLoaderService.bindAssetFromFile(IMAGE, fileName.get());
            case URL -> ResourceLoaderService.bindAssetFromURL(IMAGE, url.get());
        }
    }
}
