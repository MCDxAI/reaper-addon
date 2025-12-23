package me.ghosttypes.reaper.util.misc;

import net.minecraft.util.math.Vec3d;

/**
 * Wrapper class for storing position (Vec3d) along with rotation angles (yaw, pitch).
 * Used primarily for FakePlayer recording/playback functionality.
 *
 * @author GhostTypes
 */
public class AnglePos {
    private final Vec3d vec;
    private final float yaw, pitch;

    public AnglePos(Vec3d vec, float yaw, float pitch) {
        this.vec = vec;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vec3d getPos() {
        return vec;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
