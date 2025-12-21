package me.ghosttypes.reaper.util.player;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Player interaction utilities
 * TODO: Full implementation to be ported later
 */
public class Interactions {

    public static boolean isInElytra() {
        return mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && mc.player.isGliding();
    }
}
