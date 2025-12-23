package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.services.AuraSyncService;
import me.ghosttypes.reaper.util.services.NotificationManager;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;

/**
 * HUD element that displays notifications from the NotificationManager.
 * Supports chroma animation, background rendering, and side accents.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Notifications extends HudElement {
    public static final HudElementInfo<Notifications> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "notifications",
        "Display notifications",
        Notifications::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Rainbow notifications.")
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
        .description("Chroma animation speed.")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .visible(chroma::get)
        .build()
    );

    public final Setting<Boolean> drawBack = sgGeneral.add(new BoolSetting.Builder()
        .name("render-background")
        .description("Render a background behind notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> drawSide = sgGeneral.add(new BoolSetting.Builder()
        .name("render-side")
        .description("Render outlines on the sides of notifications.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color.")
        .defaultValue(new SettingColor(50, 50, 50))
        .build()
    );

    public final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Side accent color.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    private static final RainbowColor RAINBOW = new RainbowColor();

    public Notifications() {
        super(INFO);
    }

    public static ArrayList<String> getNotifications() {
        ArrayList<String> notifs = new ArrayList<>();
        NotificationManager.getNotifications().forEach(notification -> notifs.add(notification.text));
        return notifs;
    }

    @Override
    public void tick(HudRenderer renderer) {
        double width = 0;
        double height = 0;
        int i = 0;

        if (NotificationManager.getNotifications().isEmpty()) {
            String t = "Notifications";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
            setSize(width, height);
            return;
        } else {
            ArrayList<String> notifs = new ArrayList<>();
            NotificationManager.getNotifications().forEach(notification -> notifs.add(notification.text));
            for (String n : notifs) {
                width = Math.max(width, renderer.textWidth(n));
                height += renderer.textHeight();
                if (i > 0) height += 2;
                i++;
            }
        }
        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        Hud hud = Hud.get();
        var color = hud.textColors.get().get(0);

        double renderX = x;
        double renderY = y;
        int i = 0;

        if (isInEditor()) {
            renderer.text("Notifications", renderX, renderY, color, false);
            return;
        }

        if (NotificationManager.getNotifications().isEmpty()) return;

        ArrayList<String> notifs = new ArrayList<>();
        NotificationManager.getNotifications().forEach(notification -> notifs.add(notification.text));

        // Setup chroma colors
        RAINBOW.setSpeed(chromaSpeed.get() / 100);
        Color chromaColor = RAINBOW.getNext(renderer.delta);
        if (AuraSyncService.isEnabled()) chromaColor = AuraSyncService.RGB_COLOR;

        Color sideC = chroma.get() ? chromaColor : sideColor.get();
        Color textColor = chromaText.get() ? chromaColor : color;

        for (String n : notifs) {
            double textWidth = renderer.textWidth(n);
            double textHeight = renderer.textHeight();
            double alignedX = renderX + alignX(textWidth, Alignment.Auto);

            // Draw side accent
            if (drawSide.get()) {
                renderer.quad(alignedX - 6, renderY - 4, TextRenderer.get().getWidth(n) + 10, textHeight, sideC);
            }

            // Draw background
            if (drawBack.get()) {
                renderer.quad(alignedX - 2, renderY - 4, TextRenderer.get().getWidth(n) + 2, textHeight, backColor.get());
            }

            // Draw text
            renderer.text(n, alignedX, renderY, textColor, false);

            renderY += textHeight;
            if (i > 0) renderY += 2;
            i++;
        }
    }

    // Static helper methods for toasts
    public static void spotify(String artist, String track) {
        MeteorToast toast = new MeteorToast.Builder(artist).icon(Items.NOTE_BLOCK).text(track).build();
        MeteorClient.mc.getToastManager().add(toast);
    }

    public static void lowArmor(Item armorPiece, String text) {
        MeteorToast toast = new MeteorToast.Builder("Armor Alert").icon(armorPiece).text(text).build();
        MeteorClient.mc.getToastManager().add(toast);
    }

    public static void popAlert(String p) {
        MeteorToast toast = new MeteorToast.Builder("PopCounter").icon(Items.TOTEM_OF_UNDYING).text(p).build();
        MeteorClient.mc.getToastManager().add(toast);
    }
}
