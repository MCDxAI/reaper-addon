package me.ghosttypes.reaper.util.misc;

import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MathUtil {

    public static int intToTicks(int i) {
        return i * 20;
    }

    public static int ticksToInt(int i) {
        return i / 20;
    }

    public static double roundDouble(double d) {
        return Math.ceil(d);
    }

    public static long msPassed(long start) {
        return System.currentTimeMillis() - start;
    }

    public static long secondsPassed(long start) {
        return msToSeconds(msPassed(start));
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static String timeElapsed(long start) {
        return formatDuration(System.currentTimeMillis() - start, "HH:mm:ss");
    }

    public static String hoursElapsed(long start) {
        return formatDuration(System.currentTimeMillis() - start, "HH");
    }

    public static String minutesElapsed(long start) {
        return formatDuration(System.currentTimeMillis() - start, "mm");
    }

    public static String secondsElapsed(long start) {
        return formatDuration(System.currentTimeMillis() - start, "ss");
    }

    public static String millisElapsed(long start) {
        return Math.round(MathUtil.msPassed(start) * 100.0) / 100.0 + "ms";
    }

    public static long secondsToMS(int seconds) {
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    public static long msToSeconds(long ms) {
        return TimeUnit.MILLISECONDS.toSeconds(ms);
    }

    public static int msToTicks(long ms) {
        return intToTicks((int) msToSeconds(ms));
    }

    public static Vec3d getVelocity(PlayerEntity player) {
        return player.getVelocity();
    }

    public static BlockPos offsetByVelocity(BlockPos pos, PlayerEntity player) {
        if (pos == null || player == null) return null;
        Vec3d velocity = getVelocity(player);
        return pos.add((int) velocity.x, (int) velocity.y, (int) velocity.z);
    }

    public static BlockPos generatePredict(BlockPos pos, PlayerEntity player, int ticks) {
        if (pos == null || player == null) return null;
        Vec3d velocity = getVelocity(player);
        Vec3i v = new Vec3i((int) velocity.x * ticks, (int) velocity.y * ticks, (int) velocity.z * ticks);
        return pos.add(v);
    }

    public static boolean intersects(Box box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator());
    }

    public static boolean intersects(BlockPos pos) {
        return intersects(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()));
    }

    public static boolean intersectsAbove(BlockPos pos) {
        return intersects(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ()));
    }

    public static double[] directionSpeed(float speed) {
        // Get movement input using new 1.21.11 API
        float forward = mc.player.input.getMovementInput().y;
        float side = mc.player.input.getMovementInput().x;
        float yaw = mc.player.lastYaw + (mc.player.getYaw() - mc.player.lastYaw);

        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;

        return new double[]{posX, posZ};
    }

    // Simple duration formatter using Java standard library (replaces Apache Commons)
    public static String formatDuration(long durationMillis, String format) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;

        return switch (format) {
            case "HH:mm:ss" -> String.format("%02d:%02d:%02d", hours, minutes, seconds);
            case "HH" -> String.format("%02d", hours);
            case "mm" -> String.format("%02d", minutes);
            case "ss" -> String.format("%02d", seconds);
            default -> String.format("%02d:%02d:%02d", hours, minutes, seconds);
        };
    }
}
