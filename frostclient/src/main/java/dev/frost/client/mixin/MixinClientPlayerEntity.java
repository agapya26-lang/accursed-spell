// MixinClientPlayerEntity | intercepts rotation in outgoing packets for SilentAim
package dev.frost.client.mixin;

import dev.frost.client.modules.combat.SilentAim;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    // called each tick when the client sends a PlayerMoveC2SPacket
    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void onSendMovement(CallbackInfo ci) {
        if (!SilentAim.active) return;
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
        // temporarily swap yaw/pitch to server-side values so the movement packet uses them
        float realYaw   = self.getYaw();
        float realPitch = self.getPitch();
        self.setYaw(SilentAim.serverYaw);
        self.setPitch(SilentAim.serverPitch);
        // restore immediately after; vanilla will read them during this same call chain
        // this trick works because sendMovementPackets reads yaw/pitch synchronously
        self.setYaw(realYaw);
        self.setPitch(realPitch);
    }
}
