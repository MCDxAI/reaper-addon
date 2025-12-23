package me.ghosttypes.reaper.mixin;

import me.ghosttypes.reaper.systems.ReaperConfig;
import me.ghosttypes.reaper.util.services.NotificationManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Redirects Meteor Client (and other addons') info/warning/error messages to notifications.
 * This allows intercepting ChatUtils messages and displaying them as Reaper notifications.
 * Reaper modules automatically use notifications via ReaperModule.
 */
@Mixin(ChatUtils.class)
public class NotificationProxy {

    @Inject(method = "info(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void infoProxy(String message, Object[] args, CallbackInfo ci) {
        ReaperConfig config = ReaperConfig.get();
        if (config == null) return;

        if (config.info.get()) {
            NotificationManager.addNotification(String.format(message, args));
        }
        if (config.hide.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "warning(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void warningProxy(String message, Object[] args, CallbackInfo ci) {
        ReaperConfig config = ReaperConfig.get();
        if (config == null) return;

        if (config.warning.get()) {
            NotificationManager.addNotification(String.format(message, args));
        }
        if (config.hide.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "error(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void errorProxy(String message, Object[] args, CallbackInfo ci) {
        ReaperConfig config = ReaperConfig.get();
        if (config == null) return;

        if (config.error.get()) {
            NotificationManager.addNotification(String.format(message, args));
        }
        if (config.hide.get()) {
            ci.cancel();
        }
    }
}
