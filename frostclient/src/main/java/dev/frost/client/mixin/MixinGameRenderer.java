// MixinGameRenderer | injects render event + PlayerESP/PearlPredictor hooks
package dev.frost.client.mixin;

import dev.frost.client.FrostClient;
import dev.frost.client.event.EventBus;
import dev.frost.client.event.FrostEvent;
import dev.frost.client.modules.visuals.PlayerESP;
import dev.frost.client.modules.visuals.PearlPredictor;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (FrostClient.eventBus != null)
            FrostClient.eventBus.fire(EventBus.Type.RENDER, new FrostEvent.RenderEvent(tickDelta));
    }
}
