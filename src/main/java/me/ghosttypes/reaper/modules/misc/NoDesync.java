package me.ghosttypes.reaper.modules.misc;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Prevents ghost block placements and desync issues.
 * Ported from 1.19.4 to 1.21.11.
 */
public class NoDesync extends ReaperModule {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> breakBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("break")
        .description("Anti-desync for block breaking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> placeBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("place")
        .description("Anti-desync for block placing.")
        .defaultValue(true)
        .build()
    );

    public NoDesync() {
        super(Reaper.CATEGORY, "no-desync", "Prevent ghost block placements.");
    }

    @EventHandler
    private void onBlockPlace(InteractBlockEvent event) {
        if (!placeBlock.get()) return;
        if (mc.interactionManager == null || mc.world == null) return;

        BlockPos placePos = event.result.getBlockPos();
        if (placePos != null) {
            // Updated for 1.21.11: STOP_DESTROY_BLOCK requires sendSequencedPacket
            mc.interactionManager.sendSequencedPacket(mc.world,
                (sequence) -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, placePos, Direction.UP, sequence));
        }
    }

    @EventHandler
    private void onBlockBreak(BreakBlockEvent event) {
        if (!breakBlock.get()) return;
        if (mc.interactionManager == null || mc.world == null) return;

        BlockPos breakPos = event.blockPos;
        if (breakPos != null) {
            // Updated for 1.21.11: STOP_DESTROY_BLOCK requires sendSequencedPacket
            mc.interactionManager.sendSequencedPacket(mc.world,
                (sequence) -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, breakPos, Direction.UP, sequence));
        }
    }
}
