package me.ghosttypes.reaper.mixin;

import me.ghosttypes.reaper.Reaper;
import me.ghosttypes.reaper.util.ReaperIconResourcePack;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        String title = "Reaper " + Reaper.VERSION + " | Minecraft " + SharedConstants.getGameVersion().name();
        cir.setReturnValue(title);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
    private void setCustomIcon(Window window, ResourcePack resourcePack, Icons icons) throws IOException {
        // Wrap the resource pack to intercept icon requests and serve our custom Reaper icons
        ResourcePack wrappedPack = new ReaperIconResourcePack(resourcePack);
        window.setIcon(wrappedPack, icons);
    }
}
