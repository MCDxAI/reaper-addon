package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.Formatter;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.util.ArrayList;

/**
 * HUD element that displays a list of recent player kills.
 * Shows kill feed with player names and timestamps.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Killfeed extends HudElement {
    public static final HudElementInfo<Killfeed> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "killfeed",
        "Display a list of your kills",
        Killfeed::new
    );

    private final ArrayList<String> feed = new ArrayList<>();

    public Killfeed() {
        super(INFO);
    }

    private void updateFeed() {
        feed.clear();
        if (Formatter.hasKillFeed()) {
            feed.addAll(Formatter.getKillFeed());
        }
    }

    @Override
    public void tick(HudRenderer renderer) {
        updateFeed();
        double width = 0;
        double height = 0;
        int i = 0;

        if (feed.isEmpty()) {
            String t = "Killfeed";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
        } else {
            width = Math.max(width, renderer.textWidth("Killfeed"));
            height += renderer.textHeight();
            for (String bind : feed) {
                width = Math.max(width, renderer.textWidth(bind));
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
        updateFeed();

        double renderX = x;
        double renderY = y;

        if (isInEditor()) {
            renderer.text("Killfeed", renderX, renderY, color, false);
            return;
        }

        int i = 0;
        if (feed.isEmpty()) {
            String t = "Killfeed";
            renderer.text(t, renderX + alignX(renderer.textWidth(t), Alignment.Auto), renderY, color, false);
        } else {
            renderer.text("Killfeed", renderX + alignX(renderer.textWidth("Killfeed"), Alignment.Auto), renderY, color, false);
            renderY += renderer.textHeight();
            for (String bind : feed) {
                renderer.text(bind, renderX + alignX(renderer.textWidth(bind), Alignment.Auto), renderY, color, false);
                renderY += renderer.textHeight();
                if (i > 0) renderY += 2;
                i++;
            }
        }
    }
}
