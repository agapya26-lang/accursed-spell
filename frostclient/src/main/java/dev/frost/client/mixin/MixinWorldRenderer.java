// MixinWorldRenderer | injects ESP and PearlPredictor rendering
package dev.frost.client.mixin;

import dev.frost.client.modules.visuals.PearlPredictor;
import dev.frost.client.modules.visuals.PlayerESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Frustum;)V", shift = At.Shift.AFTER))
    private void afterEntities(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Camera cam = mc.gameRenderer.getCamera();
        double cx = cam.getPos().x, cy = cam.getPos().y, cz = cam.getPos().z;

        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate immediate =
            mc.getBufferBuilders().getEntityVertexConsumers();

        if (PlayerESP.active && PlayerESP.instance != null)
            PlayerESP.instance.renderESP(matrices, immediate, cx, cy, cz, mc);

        if (PearlPredictor.active && PearlPredictor.instance != null)
            PearlPredictor.instance.render(matrices, immediate, cx, cy, cz, mc);

        immediate.draw();
    }
}
