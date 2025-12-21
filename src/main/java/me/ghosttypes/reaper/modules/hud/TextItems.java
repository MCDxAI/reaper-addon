package me.ghosttypes.reaper.modules.hud;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.player.Interactions;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * HUD element that displays the count of selected items in inventory.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class TextItems extends HudElement {
    public static final HudElementInfo<TextItems> INFO = new HudElementInfo<>(
        Reaper.HUD_GROUP,
        "item-counter",
        "Display the amount of selected items in your inventory.",
        TextItems::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode")
        .description("How to sort the items list.")
        .defaultValue(SortMode.Shortest)
        .build()
    );

    private final Setting<Boolean> beds = sgGeneral.add(new BoolSetting.Builder()
        .name("beds")
        .description("Show count of all bed types.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Which items to display in the counter list.")
        .defaultValue(new ArrayList<>(0))
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

    private final ArrayList<String> itemCounter = new ArrayList<>();

    public TextItems() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double textScale = getScale();

        if (!Utils.canUpdate()) {
            String displayText = "Item Counters";
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text(displayText, x, y, textColor.get(), true, textScale);
            return;
        }

        updateCounter();

        if (isInEditor() || itemCounter.isEmpty()) {
            String displayText = itemCounter.isEmpty() ? "Item Counters" : itemCounter.get(0);
            setSize(renderer.textWidth(displayText, true, textScale), renderer.textHeight(true, textScale));
            renderer.text("Item Counters", x, y, textColor.get(), true, textScale);
            return;
        }

        // Calculate dimensions
        double width = 0;
        double height = 0;
        for (int i = 0; i < itemCounter.size(); i++) {
            width = Math.max(width, renderer.textWidth(itemCounter.get(i), true, textScale));
            height += renderer.textHeight(true, textScale);
            if (i > 0) height += 2;
        }
        setSize(width, height);

        // Render items
        double currentY = y;
        for (int i = 0; i < itemCounter.size(); i++) {
            String counter = itemCounter.get(i);
            renderer.text(counter, x, currentY, textColor.get(), true, textScale);
            currentY += renderer.textHeight(true, textScale);
            if (i > 0) currentY += 2;
        }
    }

    private void updateCounter() {
        itemCounter.clear();
        for (Item item : items.get()) {
            if (!(item instanceof BedItem)) {
                itemCounter.add(Interactions.getCommonName(item) + ": " + InvUtils.find(item).count());
            }
        }
        if (beds.get()) itemCounter.add("Beds: " + Interactions.bedCount());

        switch (sortMode.get()) {
            case Shortest -> itemCounter.sort(Comparator.comparing(String::length));
            case Longest -> itemCounter.sort(Comparator.comparing(String::length).reversed());
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
