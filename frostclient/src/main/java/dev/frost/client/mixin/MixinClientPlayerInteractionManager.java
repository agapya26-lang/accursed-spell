// MixinClientPlayerInteractionManager | FastPlace delay bypass
package dev.frost.client.mixin;

import dev.frost.client.modules.automation.FastPlace;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    // isBlockPlacingCooldown controls the 4-tick delay between placements
    @Inject(method = "isBlockPlacingCooldown", at = @At("HEAD"), cancellable = true)
    private void noPlaceCooldown(CallbackInfoReturnable<Boolean> cir) {
        if (FastPlace.noPlaceDelay) cir.setReturnValue(false);
    }
}
