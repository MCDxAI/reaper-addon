package me.ghosttypes.reaper.util.combat;

import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Utility for breaking surround blocks and finding crystal placement positions.
 * Used by AntiSurround and similar combat modules.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class CityUtils {

    /**
     * Find the closest obsidian block surrounding the target player.
     * This is used to determine which block to break for "city" attacks.
     *
     * @param target The player to find surround blocks around
     * @return The closest obsidian block position, or null if none found
     */
    public static BlockPos getBreakPos(PlayerEntity target) {
        if (target == null) return null;

        ArrayList<BlockPos> blockPos = new ArrayList<>();
        BlockPos targetPos = target.getBlockPos();

        for (Direction direction : Direction.values()) {
            if (direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) continue;

            if (mc.world.getBlockState(targetPos.offset(direction)).isOf(Blocks.OBSIDIAN)) {
                blockPos.add(targetPos.offset(direction));
            }
        }

        blockPos.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return blockPos.isEmpty() ? null : blockPos.get(0);
    }

    /**
     * Find the best position to place a crystal next to the break position.
     * Checks horizontal positions around the break block.
     *
     * @param breakPos The position of the block being broken
     * @param support Whether to allow placing crystals with air below (support block)
     * @return The crystal placement position (on the floor), or null if none valid
     */
    public static BlockPos getCrystalPos(BlockPos breakPos, boolean support) {
        if (breakPos == null) return null;
        ArrayList<BlockPos> blockPos = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) continue;

            if (canCrystal(breakPos.offset(direction), support)) {
                blockPos.add(breakPos.offset(direction));
            }
        }

        blockPos.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return blockPos.isEmpty() ? null : blockPos.get(0).down();
    }

    /**
     * Check if a crystal can be placed at the given position.
     * Ensures no entities are blocking and the floor is valid.
     *
     * @param blockPos Position to check (the space above the floor)
     * @param support Whether to allow placing with air or obsidian below
     * @return True if crystal can be placed
     */
    private static boolean canCrystal(BlockPos blockPos, boolean support) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof ItemEntity) {
                if (entity.getBlockPos().equals(blockPos)) return false;
            }
        }

        return mc.world.getBlockState(blockPos).isOf(Blocks.AIR) &&
            (support ?
                (mc.world.getBlockState(blockPos.down()).isOf(Blocks.AIR) ||
                 mc.world.getBlockState(blockPos.down()).isOf(Blocks.OBSIDIAN) ||
                 mc.world.getBlockState(blockPos.down()).isOf(Blocks.BEDROCK)) :
                (mc.world.getBlockState(blockPos.down()).isOf(Blocks.OBSIDIAN) ||
                 mc.world.getBlockState(blockPos.down()).isOf(Blocks.BEDROCK)));
    }

    /**
     * Get the best direction to interact with a block from the player's position.
     * Uses raycasting to determine which face is visible.
     *
     * @param pos The block position to interact with
     * @return The direction to interact with, or UP/DOWN as fallback
     */
    public static Direction getDirection(BlockPos pos) {
        if (pos == null) return Direction.UP;

        Vec3d eyesPos = new Vec3d(
            mc.player.getX(),
            mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
            mc.player.getZ()
        );

        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(
                eyesPos,
                new Vec3d(
                    pos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                    pos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                    pos.getZ() + 0.5 + direction.getVector().getZ() * 0.5
                ),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
            );

            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if ((double) pos.getY() > eyesPos.y) {
            return Direction.DOWN;
        }
        return Direction.UP;
    }
}
