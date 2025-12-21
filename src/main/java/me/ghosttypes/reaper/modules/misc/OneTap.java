package me.ghosttypes.reaper.modules.misc;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.misc.ReaperModule;
import me.ghosttypes.reaper.util.player.Interactions;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

/**
 * One-tap bow exploit using packet spoofing.
 * Allows instant bow shots, eggs, pearls, and snowballs.
 *
 * Credit to Murphy for porting the base module.
 * Ported from 1.19.4 to 1.21.11.
 */
public class OneTap extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General
    private final Setting<Boolean> bows = sgGeneral.add(new BoolSetting.Builder().name("bows").defaultValue(true).build());
    private final Setting<Boolean> pearls = sgGeneral.add(new BoolSetting.Builder().name("pearl").defaultValue(true).build());
    private final Setting<Boolean> eggs = sgGeneral.add(new BoolSetting.Builder().name("eggs").defaultValue(true).build());
    private final Setting<Boolean> snowballs = sgGeneral.add(new BoolSetting.Builder().name("snowballs").defaultValue(true).build());
    private final Setting<Boolean> sfx = sgGeneral.add(new BoolSetting.Builder().name("railgun-sfx").defaultValue(true).build());
    public final Setting<Double> sfxVolume = sgGeneral.add(new DoubleSetting.Builder().name("sfx-volume").description("How loud the sound is.").defaultValue(1).min(1).sliderMax(5).build());
    private final Setting<Integer> timeout = sgGeneral.add(new IntSetting.Builder().name("timeout").min(0).max(20000).sliderMin(100).sliderMax(20000).defaultValue(1).build());
    private final Setting<Integer> spoofs = sgGeneral.add(new IntSetting.Builder().name("spoofs").min(0).max(300).sliderMin(1).sliderMax(300).defaultValue(100).build());
    private final Setting<Boolean> bypass = sgGeneral.add(new BoolSetting.Builder().name("bypass").defaultValue(false).build());
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder().name("debug").defaultValue(false).build());

    public OneTap() {
        super(Reaper.CATEGORY, "one-tap", "one tap bow exploit.");
    }

    private long lastShootTime;


    @Override
    public void onActivate() {
        lastShootTime = System.currentTimeMillis();
    }

    // Exploit
    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket actionPacket) {
            if (actionPacket.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
                ItemStack handStack = mc.player.getStackInHand(Hand.MAIN_HAND);
                if (!handStack.isEmpty() && handStack.getItem() != null && handStack.getItem() instanceof BowItem && bows.get()) {
                    if (sfx.get()) {
                        float volume = sfxVolume.get().floatValue();
                        mc.player.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, volume, 1.0f);
                    }
                    doSpoofs();
                    if (debug.get()) ChatUtils.info(name, "trying to spoof");
                }
            }
        }
        if (event.packet instanceof PlayerInteractItemC2SPacket itemPacket) {
            if (itemPacket.getHand() == Hand.MAIN_HAND) {
                Item handItem = Interactions.getMainHandItem();
                if (handItem == null) return;
                if (handItem instanceof EggItem && eggs.get() || handItem instanceof EnderPearlItem && pearls.get() || handItem instanceof SnowballItem && snowballs.get()) doSpoofs();
            }
        }
    }


    private void doSpoofs() {
        if (System.currentTimeMillis() - lastShootTime >= timeout.get()) {
            lastShootTime = System.currentTimeMillis();

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

            for (int index = 0; index < spoofs.get(); ++index) {
                if (bypass.get()) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1e-10, mc.player.getZ(), true, false));
                } else {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1e-10, mc.player.getZ(), true, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false, false));
                }

            }

            if (debug.get()) ChatUtils.info(name, "spoofed");
        }
    }
}
