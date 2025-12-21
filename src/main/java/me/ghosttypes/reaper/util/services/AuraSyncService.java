package me.ghosttypes.reaper.util.services;

import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;

/**
 * Service that synchronizes RGB/rainbow colors across all Reaper HUD elements.
 * Provides a unified chroma color that updates each frame.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class AuraSyncService {

    public static HudElement auraSync = null;
    public static final RainbowColor RAINBOW = new RainbowColor();
    public static Color RGB_COLOR = RAINBOW.getNext();

    /**
     * Initialize the service by finding the AuraSync HUD element.
     */
    public static void init() {
        Hud hud = Hud.get();
        for (HudElement element : hud) {
            if (element.isActive() && element.info.name.equalsIgnoreCase("aura-sync")) {
                auraSync = element;
                break;
            }
        }
    }

    /**
     * Check if AuraSync is enabled.
     * @return true if the AuraSync HUD element is active
     */
    public static boolean isEnabled() {
        if (auraSync == null) {
            // Try to find it again in case it was added after init
            init();
            return false;
        }
        return auraSync.isActive();
    }

    /**
     * Get the next rainbow color.
     * @return the next color in the rainbow sequence
     */
    public static Color getNext() {
        return RAINBOW.getNext();
    }

    /**
     * Get the next rainbow color with delta time.
     * @param delta the time delta for smooth animation
     * @return the next color in the rainbow sequence
     */
    public static Color getNext(double delta) {
        return RAINBOW.getNext(delta);
    }

    /**
     * Set the rainbow animation speed.
     * @param speed the speed value (will be divided by 100)
     */
    public static void setSpeed(double speed) {
        RAINBOW.setSpeed(speed / 100);
    }
}
