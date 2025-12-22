package me.ghosttypes.reaper.modules.combat;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import me.ghosttypes.reaper.util.player.Interactions;
import me.ghosttypes.reaper.util.world.RotationHelper;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetStrafe extends ReaperModule {

    public enum MoveMode { Basic, Scroll }
    public enum ExecuteMode { Tick, Move }

    public TargetStrafe() {
        super(Reaper.CATEGORY, "target-strafe", "Automatically circle your target.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<MoveMode> mode = sgGeneral.add(new EnumSetting.Builder<MoveMode>()
        .name("mode")
        .description("Movement calculation mode.")
        .defaultValue(MoveMode.Basic)
        .build()
    );

    public final Setting<ExecuteMode> executeMode = sgGeneral.add(new EnumSetting.Builder<ExecuteMode>()
        .name("execute-mode")
        .description("When to execute the strafe logic.")
        .defaultValue(ExecuteMode.Tick)
        .build()
    );

    public final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("Maximum range to find targets.")
        .defaultValue(7)
        .sliderRange(1, 30)
        .build()
    );

    public final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Circle radius around target.")
        .defaultValue(1.9)
        .sliderRange(1, 30)
        .build()
    );

    public final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Movement speed.")
        .defaultValue(0.24)
        .sliderRange(0.1, 1)
        .build()
    );

    public final Setting<Double> scrollSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("scroll-speed")
        .description("Scroll mode movement speed.")
        .defaultValue(0.26)
        .sliderRange(0.1, 1)
        .visible(() -> mode.get() == MoveMode.Scroll)
        .build()
    );

    public final Setting<Boolean> damageBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("damage-boost")
        .description("Speed boost when taking damage.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> boost = sgGeneral.add(new DoubleSetting.Builder()
        .name("boost")
        .description("Amount of boost to add when damaged.")
        .defaultValue(0.09)
        .sliderRange(0, 1)
        .visible(damageBoost::get)
        .build()
    );

    private PlayerEntity target;
    private int direction = 1;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (executeMode.get() == ExecuteMode.Tick) run();
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (executeMode.get() == ExecuteMode.Move) run();
    }

    private void run() {
        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.ClosestAngle);
        if (TargetUtils.isBadTarget(target, targetRange.get())) return;

        // Jump if on ground, set direction based on input
        if (mc.player.isOnGround()) mc.player.jump();
        if (mc.options.leftKey.isPressed()) {
            direction = 1;
        } else if (mc.options.rightKey.isPressed()) {
            direction = -1;
        }

        // Reverse direction on collision
        if (mc.player.horizontalCollision) direction = direction == 1 ? -1 : 1;

        // Calculate speed with optional damage boost
        double speed = damageBoost.get() && mc.player.hurtTime != 0
            ? this.speed.get() + boost.get()
            : this.speed.get();

        // Calculate forward movement factor (move towards target if too far)
        double forward = mc.player.distanceTo(target) > radius.get() ? 1 : 0;

        // Look at target
        float yaw = RotationHelper.lookAtEntity(target)[0];
        mc.player.bodyYaw = yaw;
        mc.player.headYaw = yaw;

        // Execute movement mode
        switch (mode.get()) {
            case Basic -> getBasic(yaw, speed, forward, direction);
            case Scroll -> getScroll(target, speed);
        }
    }

    private void getScroll(Entity target, double speed) {
        double c1 = (mc.player.getX() - target.getX()) / (Math.sqrt(Math.pow(mc.player.getX() - target.getX(), 2) + Math.pow(mc.player.getZ() - target.getZ(), 2)));
        double s1 = (mc.player.getZ() - target.getZ()) / (Math.sqrt(Math.pow(mc.player.getX() - target.getX(), 2) + Math.pow(mc.player.getZ() - target.getZ(), 2)));
        double x = speed * s1 * direction - scrollSpeed.get() * speed * c1;
        double z = -speed * c1 * direction - scrollSpeed.get() * speed * s1;
        Interactions.setHVelocity(x, z);
    }

    private void getBasic(float yaw, double speed, double forward, double direction) {
        if (forward != 0.0D) {
            if (direction > 0.0D) {
                yaw += (float) (forward > 0.0D ? -45 : 45);
            } else if (direction < 0.0D) {
                yaw += (float) (forward > 0.0D ? 45 : -45);
            }
            direction = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }

        double cos = Math.cos(Math.toRadians((yaw + 90.0F)));
        double sin = Math.sin(Math.toRadians((yaw + 90.0F)));

        double x = forward * speed * cos + direction * speed * sin;
        double z = forward * speed * sin - direction * speed * cos;
        Interactions.setHVelocity(x, z);
    }
}
