package me.ghosttypes.reaper.util.network;

import me.ghosttypes.reaper.util.player.Interactions;
import me.ghosttypes.reaper.util.services.TL;
import me.ghosttypes.reaper.util.world.CombatHelper;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Packet sending utilities for combat and interaction modules.
 * Ported from 1.19.4 to 1.21.11.
 */
public class PacketManager {

    // Packet Mining
    public static void sendStartDestroy(BlockPos pos) {
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
    }

    public static void sendStopDestroy(BlockPos pos) {
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
    }

    public static void sendAbortDestroy(BlockPos pos) {
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP));
    }

    public static void startPacketMine(BlockPos pos, boolean sendSwing, boolean offhand) {
        if (pos == null) return;
        sendStartDestroy(pos);
        if (sendSwing) swingHand(offhand);
        sendStopDestroy(pos);
    }

    public static void finishPacketMine(BlockPos pos, boolean sendSwing, boolean offhand) {
        if (pos == null) return;
        sendAbortDestroy(pos);
        if (sendSwing) swingHand(offhand);
    }

    public static void abortPacketMine(BlockPos pos) {
        if (pos == null) return;
        sendAbortDestroy(pos);
    }

    public static void swingHand(boolean offhand) {
        sendPacket(new HandSwingC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
    }

    // Inventory
    public static void updateSlot(int slot) {
        if (slot == -1) return;
        sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    // Sending Packets
    public static void sendPacket(Packet<?> packet) {
        if (packet == null || mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static void sendMovementPacket(double x, double y, double z, boolean onGround) {
        if (x == -1 || y == -1 || z == -1) return;
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, mc.player.horizontalCollision));
    }

    public static void sendAttackPacket(Entity entity, boolean isSneaking) {
        if (entity == null) return;
        sendPacket(PlayerInteractEntityC2SPacket.attack(entity, isSneaking));
    }

    public static void clipUp(int i) {
        if (i == -1) return;
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + i, mc.player.getZ(), false, mc.player.horizontalCollision));
    }

    // Rotations
    public static void rotate(double yaw, double pitch, boolean onGround) {
        if (yaw == -1 || pitch == -1) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) yaw, (float) pitch, onGround, mc.player.horizontalCollision));
    }

    public static void rotate(BlockPos pos, boolean onGround) {
        if (pos == null) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) Rotations.getYaw(pos), (float) Rotations.getPitch(pos), onGround, mc.player.horizontalCollision));
    }

    public static void rotate(BlockPos pos, Runnable task) {
        if (pos == null) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) Rotations.getYaw(pos), (float) Rotations.getPitch(pos), mc.player.isOnGround(), mc.player.horizontalCollision));
        task.run();
    }

    // Item Interactions

    /**
     * Use an item via the interaction manager.
     * Updated for 1.21.11: uses interactionManager instead of raw packet.
     */
    public static void interactItem(Hand hand) {
        if (mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.interactItem(mc.player, hand);
    }

    // Block Interactions

    public static ArrayList<BlockHitResult> pendingPlaces = new ArrayList<>();

    public static void sendInteract(Hand hand, FindItemResult item, BlockHitResult hitResult, boolean rotate, boolean packet) {
        if (hand == null || item == null || hitResult == null || !item.found() || pendingPlaces.contains(hitResult)) return;
        if (hand == Hand.MAIN_HAND && !Interactions.isHolding(item)) Interactions.setSlot(item.slot(), false);
        BlockPos pos = hitResult.getBlockPos();
        if (rotate) {
            if (CombatHelper.isInHole(mc.player)) rotate(pos, () -> sendInteract(hand, hitResult, packet));
            else Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), () -> sendInteract(hand, hitResult, packet));
        } else sendInteract(hand, hitResult, packet);
    }

    public static void sendInteract(Hand hand, BlockHitResult result, boolean packet) {
        TL.modules.execute(() -> {
            pendingPlaces.add(result);
            try { Thread.sleep(60); } catch (Exception ignored) {}
            pendingPlaces.remove(result);
        });
        if (packet) {
            // Updated for 1.21.11: use sendSequencedPacket for proper sequence handling
            if (mc.interactionManager != null && mc.world != null) {
                mc.interactionManager.sendSequencedPacket(mc.world,
                    (sequence) -> new PlayerInteractBlockC2SPacket(hand, result, sequence));
            }
            swingHand(hand == Hand.OFF_HAND);
        } else {
            // client placing
            mc.interactionManager.interactBlock(mc.player, hand, result);
            mc.player.swingHand(hand);
        }
    }

    public static void sendBurrow() {
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4, mc.player.getZ(), true, mc.player.horizontalCollision));
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.75, mc.player.getZ(), true, mc.player.horizontalCollision));
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.01, mc.player.getZ(), true, mc.player.horizontalCollision));
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), true, mc.player.horizontalCollision));
    }
}
