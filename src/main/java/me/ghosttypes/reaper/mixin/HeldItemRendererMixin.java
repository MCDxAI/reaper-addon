package me.ghosttypes.reaper.mixin;

import me.ghosttypes.reaper.events.UpdateHeldItemEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject into HeldItemRenderer.updateHeldItems() and fire UpdateHeldItemEvent.
 * Used by OldAnimations module to override the vanilla held item update logic.
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "updateHeldItems", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateHeldItem(CallbackInfo ci) {
        HeldItemRenderer heldItemRenderer = (HeldItemRenderer) (Object) this;
        if (MeteorClient.EVENT_BUS.post(UpdateHeldItemEvent.get(heldItemRenderer)).isCancelled()) {
            ci.cancel();
        }
    }
}
