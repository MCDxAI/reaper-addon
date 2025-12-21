package me.ghosttypes.reaper.modules.chat;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import me.ghosttypes.reaper.util.player.Interactions;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class ArmorAlert extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability")
        .description("How low an armor piece needs to be to alert you.")
        .defaultValue(2)
        .min(1)
        .sliderMin(1)
        .sliderMax(100)
        .max(100)
        .build());

    public ArmorAlert() {
        super(Reaper.CATEGORY, "armor-alert", "Alerts you when your armor pieces are low.");
    }

    private boolean alertedHelm;
    private boolean alertedChest;
    private boolean alertedLegs;
    private boolean alertedBoots;

    @Override
    public void onActivate() {
        alertedHelm = false;
        alertedChest = false;
        alertedLegs = false;
        alertedBoots = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Check each armor slot using Interactions.getArmor(slot)
        // Slot indices: 0=feet, 1=legs, 2=chest, 3=head
        checkArmorPiece(Interactions.getArmor(3), "helmet", 3);
        checkArmorPiece(Interactions.getArmor(2), "chestplate", 2);
        checkArmorPiece(Interactions.getArmor(1), "leggings", 1);
        checkArmorPiece(Interactions.getArmor(0), "boots", 0);
    }

    private void checkArmorPiece(ItemStack armorPiece, String name, int slot) {
        if (armorPiece.isEmpty()) {
            resetAlert(slot);
            return;
        }

        boolean isLow = Interactions.checkThreshold(armorPiece, threshold.get());
        boolean alerted = getAlerted(slot);

        if (isLow && !alerted) {
            warning("Your " + name + " is low");
            setAlerted(slot, true);
        } else if (!isLow && alerted) {
            setAlerted(slot, false);
        }
    }

    private boolean getAlerted(int slot) {
        return switch (slot) {
            case 0 -> alertedBoots;
            case 1 -> alertedLegs;
            case 2 -> alertedChest;
            case 3 -> alertedHelm;
            default -> false;
        };
    }

    private void setAlerted(int slot, boolean value) {
        switch (slot) {
            case 0 -> alertedBoots = value;
            case 1 -> alertedLegs = value;
            case 2 -> alertedChest = value;
            case 3 -> alertedHelm = value;
        }
    }

    private void resetAlert(int slot) {
        setAlerted(slot, false);
    }
}
