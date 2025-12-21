package me.ghosttypes.reaper.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor for HeldItemRenderer to modify equip progress and hand items.
 * Used by OldAnimations module for 1.8-style animations.
 *
 * Field name changes in 1.21.11:
 * - prevEquipProgressMainHand -> lastEquipProgressMainHand
 * - prevEquipProgressOffHand -> lastEquipProgressOffHand
 */
@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor("equipProgressMainHand")
    void setEquipProgressMainHand(float equipProgressMainHand);

    @Accessor("equipProgressMainHand")
    float getEquipProgressMainHand();

    @Accessor("lastEquipProgressMainHand")
    void setLastEquipProgressMainHand(float lastEquipProgressMainHand);

    @Accessor("lastEquipProgressMainHand")
    float getLastEquipProgressMainHand();

    @Accessor("equipProgressOffHand")
    void setEquipProgressOffHand(float equipProgressOffHand);

    @Accessor("equipProgressOffHand")
    float getEquipProgressOffHand();

    @Accessor("lastEquipProgressOffHand")
    void setLastEquipProgressOffHand(float lastEquipProgressOffHand);

    @Accessor("lastEquipProgressOffHand")
    float getLastEquipProgressOffHand();

    @Accessor("mainHand")
    void setMainHand(ItemStack itemStackMainHand);

    @Accessor("mainHand")
    ItemStack getMainHand();

    @Accessor("offHand")
    void setOffHand(ItemStack itemStackOffHand);

    @Accessor("offHand")
    ItemStack getOffHand();
}
