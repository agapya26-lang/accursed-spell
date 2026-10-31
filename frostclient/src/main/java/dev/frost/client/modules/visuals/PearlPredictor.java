// PearlPredictor | Visuals | Fabric 1.21.1
// simulates ender pearl trajectory and renders it
package dev.frost.client.modules.visuals;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class PearlPredictor extends Module {

    public float r = 0.4f, g = 0.8f, b = 1.0f, a = 0.8f;
    public int   simTicks = 200;
    public static PearlPredictor instance;
    public static boolean active = false;

    public PearlPredictor() {
        super("PearlPredictor", Category.VISUALS);
        instance = this;
    }

    @Override protected void onEnable()  { active = true; }
    @Override protected void onDisable() { active = false; }

    // simulate projectile physics client-side
    private List<Vec3d> simulate(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return List.of();

        List<Vec3d> points = new ArrayList<>();
        double yaw   = Math.toRadians(mc.player.getYaw());
        double pitch = Math.toRadians(mc.player.getPitch());

        double speed = 1.5;
        double vx = -Math.sin(yaw) * Math.cos(pitch) * speed;
        double vy = -Math.sin(pitch) * speed;
        double vz =  Math.cos(yaw) * Math.cos(pitch) * speed;

        Vec3d pos = mc.player.getEyePos();
        double gravity = 0.05;
        double drag    = 0.99;

        for (int i = 0; i < simTicks; i++) {
            pos = pos.add(vx, vy, vz);
            vx *= drag; vz *= drag;
            vy = (vy - gravity) * drag;

            // very rough collision: stop if block is solid
            if (!mc.world.getBlockState(new net.minecraft.util.math.BlockPos(
                    (int)pos.x, (int)pos.y, (int)pos.z)).isAir()) break;

            points.add(pos);
        }
        return points;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                       double camX, double camY, double camZ, MinecraftClient mc) {
        List<Vec3d> pts = simulate(mc);
        if (pts.size() < 2) return;

        VertexConsumer vc = immediate.getBuffer(RenderLayer.getLines());
        Matrix4f pose = matrices.peek().getPositionMatrix();

        for (int i = 0; i < pts.size() - 1; i++) {
            Vec3d a = pts.get(i);
            Vec3d b = pts.get(i + 1);
            vc.vertex(pose, (float)(a.x - camX), (float)(a.y - camY), (float)(a.z - camZ))
              .color(this.r, this.g, this.b, this.a).normal(0, 1, 0);
            vc.vertex(pose, (float)(b.x - camX), (float)(b.y - camY), (float)(b.z - camZ))
              .color(this.r, this.g, this.b, this.a).normal(0, 1, 0);
        }
    }
}
