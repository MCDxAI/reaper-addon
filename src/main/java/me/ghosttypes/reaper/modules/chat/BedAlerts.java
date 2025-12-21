package me.ghosttypes.reaper.modules.chat;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.MathUtil;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import me.ghosttypes.reaper.util.player.Interactions;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;

import java.util.ArrayList;

public class BedAlerts extends ReaperModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("How far away to check players.")
        .defaultValue(3.5)
        .min(0)
        .sliderMax(10)
        .build());

    private final Setting<Boolean> smartTrap = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-trap")
        .description("Automatically self-trap when a bed holder is nearby.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> smartTrapHole = sgGeneral.add(new BoolSetting.Builder()
        .name("require-hole")
        .description("Require being in a hole for smart trap.")
        .defaultValue(false)
        .visible(smartTrap::get)
        .build());

    private final Setting<Double> smartTrapRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("smart-trap-range")
        .description("How close a bed holder needs to be to trigger smart trap.")
        .defaultValue(2)
        .min(0)
        .sliderMax(10)
        .visible(smartTrap::get)
        .build());

    private long lastTrap;
    private final ArrayList<PlayerEntity> bedHolders = new ArrayList<>();
    private final ArrayList<PlayerEntity> crafters = new ArrayList<>();

    public BedAlerts() {
        super(Reaper.CATEGORY, "bed-alerts", "Alerts you about nearby bed holders.");
    }

    @Override
    public void onActivate() {
        lastTrap = MathUtil.now() - 5000;
        bedHolders.clear();
        crafters.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) return;

        boolean shouldTrap = false;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity player) || player == mc.player) continue;
            if (mc.player.distanceTo(player) > range.get()) continue;

            String playerName = player.getName().getString();
            boolean holdingBed = player.getMainHandStack().getItem() instanceof BedItem
                || player.getOffHandStack().getItem() instanceof BedItem;

            if (holdingBed) {
                if (!bedHolders.contains(player)) {
                    bedHolders.add(player);
                    warning(playerName + " is holding a bed.");
                }
                if (smartTrap.get() && mc.player.distanceTo(player) < smartTrapRange.get()) {
                    shouldTrap = true;
                }

                // Check if also holding crafting table
                boolean holdingCraftingTable = player.getMainHandStack().getItem().equals(Items.CRAFTING_TABLE)
                    || player.getOffHandStack().getItem().equals(Items.CRAFTING_TABLE);

                if (holdingCraftingTable && !crafters.contains(player)) {
                    crafters.add(player);
                    warning(playerName + " is crafting beds.");
                }
            } else {
                if (bedHolders.contains(player)) {
                    info(playerName + " is no longer holding a bed.");
                    bedHolders.remove(player);
                }
                if (crafters.contains(player)) {
                    info(playerName + " is no longer crafting beds.");
                    crafters.remove(player);
                }
            }
        }

        // Smart trap logic (requires SelfTrapPlus to be ported)
        if (MathUtil.msPassed(lastTrap) > 2000 && shouldTrap) {
            if (smartTrapHole.get() && !Interactions.isInHole()) return;
            info("Nearby bed holder detected - consider self-trapping.");
            lastTrap = MathUtil.now();
            // TODO: Activate SelfTrapPlus when ported
            // SelfTrapPlus stp = Modules.get().get(SelfTrapPlus.class);
            // if (stp != null && !stp.isActive()) stp.toggle();
        }
    }
}
