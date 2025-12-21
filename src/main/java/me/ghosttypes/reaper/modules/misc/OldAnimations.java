package me.ghosttypes.reaper.modules.misc;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.events.UpdateHeldItemEvent;
import me.ghosttypes.reaper.mixin.HeldItemRendererAccessor;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

/**
 * OldAnimations module - Changes hit animations to those of 1.8 style.
 * Modifies the item swapping animation behavior for a more classic feel.
 */
public class OldAnimations extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showSwapping = sgGeneral.add(new BoolSetting.Builder()
        .name("show-swapping")
        .description("Whether or not to show the item swapping animation.")
        .defaultValue(true)
        .build());

    private static int slotMainHand = 0;

    public OldAnimations() {
        super(Reaper.CATEGORY, "old-animations", "Changes hit animations to those of 1.8.");
    }

    @EventHandler
    private void onUpdateHeldItem(UpdateHeldItemEvent event) {
        if (mc.player == null) return;

        event.setCancelled(true);
        HeldItemRendererAccessor heldItemRenderer = ((HeldItemRendererAccessor) event.renderer);
        ItemStack mainHandStack = mc.player.getMainHandStack();
        ItemStack offHandStack = mc.player.getOffHandStack();

        // Store previous equip progress
        heldItemRenderer.setLastEquipProgressMainHand(heldItemRenderer.getEquipProgressMainHand());
        heldItemRenderer.setLastEquipProgressOffHand(heldItemRenderer.getEquipProgressOffHand());

        if (mc.player.isRiding()) {
            // When riding, smoothly lower hands
            heldItemRenderer.setEquipProgressMainHand(
                MathHelper.clamp(heldItemRenderer.getEquipProgressMainHand() - 0.4F, 0.0F, 1.0F)
            );
            heldItemRenderer.setEquipProgressOffHand(
                MathHelper.clamp(heldItemRenderer.getEquipProgressOffHand() - 0.4F, 0.0F, 1.0F)
            );
        } else {
            // Check if items should trigger re-equip animation
            boolean reequipM = showSwapping.get() && shouldCauseReequipAnimation(
                heldItemRenderer.getMainHand(), mainHandStack, mc.player.getInventory().getSelectedSlot()
            );
            boolean reequipO = showSwapping.get() && shouldCauseReequipAnimation(
                heldItemRenderer.getOffHand(), offHandStack, -1
            );

            // Update hand items if not re-equipping
            if (!reequipM && !Objects.equals(heldItemRenderer.getMainHand(), mainHandStack)) {
                heldItemRenderer.setMainHand(mainHandStack);
            }
            if (!reequipO && !Objects.equals(heldItemRenderer.getOffHand(), offHandStack)) {
                heldItemRenderer.setOffHand(offHandStack);
            }

            // Smooth equip progress transition
            heldItemRenderer.setEquipProgressMainHand(
                heldItemRenderer.getEquipProgressMainHand() + MathHelper.clamp(
                    (!reequipM ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressMainHand(), -0.4F, 0.4F
                )
            );
            heldItemRenderer.setEquipProgressOffHand(
                heldItemRenderer.getEquipProgressOffHand() + MathHelper.clamp(
                    (!reequipO ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressOffHand(), -0.4F, 0.4F
                )
            );
        }

        // Force update hand items when equip progress is low
        if (heldItemRenderer.getLastEquipProgressMainHand() < 0.1F) {
            heldItemRenderer.setMainHand(mainHandStack);
        }
        if (heldItemRenderer.getEquipProgressOffHand() < 0.1F) {
            heldItemRenderer.setOffHand(offHandStack);
        }
    }

    private boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();

        if (fromInvalid && toInvalid) return false;
        if (fromInvalid || toInvalid) return true;
        if (slot != -1) slotMainHand = slot;

        return !from.equals(to);
    }
}
