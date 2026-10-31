// MixinMinecraftClient | fires tick event
package dev.frost.client.mixin;

import dev.frost.client.FrostClient;
import dev.frost.client.event.EventBus;
import dev.frost.client.event.FrostEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (FrostClient.eventBus != null) {
            FrostClient.eventBus.fire(EventBus.Type.TICK, new FrostEvent.TickEvent((MinecraftClient)(Object)this));
        }
    }
}
