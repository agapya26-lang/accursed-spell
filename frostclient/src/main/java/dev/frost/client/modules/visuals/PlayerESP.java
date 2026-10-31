// PlayerESP | Visuals | Fabric 1.21.1
// renders colored box + name through walls
package dev.frost.client.modules.visuals;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class PlayerESP extends Module {

    public float  r = 1f, g = 0.2f, b = 0.2f, a = 0.5f;   // default red
    public boolean throughWalls = true;
    public boolean showName     = true;
    public float  lineWidth     = 1.5f;

    // rendering done from MixinWorldRenderer
    public static boolean active = false;
    public static PlayerESP instance;

    public PlayerESP() {
        super("PlayerESP", Category.VISUALS);
        instance = this;
    }

    @Override protected void onEnable()  { active = true; }
    @Override protected void onDisable() { active = false; }

    // called by MixinWorldRenderer after entities render
    public void renderESP(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                          double camX, double camY, double camZ, MinecraftClient mc) {
        if (mc.world == null) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            renderBox(matrices, immediate, player, camX, camY, camZ);
        }
    }

    private void renderBox(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                           PlayerEntity player, double camX, double camY, double camZ) {
        Box box = player.getBoundingBox();
        matrices.push();
        matrices.translate(box.minX - camX, box.minY - camY, box.minZ - camZ);

        RenderLayer layer = throughWalls
            ? RenderLayer.getLines()
            : RenderLayer.getLines();

        VertexConsumer vc = immediate.getBuffer(layer);
        Matrix4f pose = matrices.peek().getPositionMatrix();

        double w = box.maxX - box.minX;
        double h = box.maxY - box.minY;
        double d = box.maxZ - box.minZ;

        drawBox(vc, pose, 0, 0, 0, (float)w, (float)h, (float)d, r, g, b, a);
        matrices.pop();
    }

    private void drawBox(VertexConsumer vc, Matrix4f m,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        // 12 edges
        line(vc, m, x1,y1,z1, x2,y1,z1, r,g,b,a);
        line(vc, m, x2,y1,z1, x2,y1,z2, r,g,b,a);
        line(vc, m, x2,y1,z2, x1,y1,z2, r,g,b,a);
        line(vc, m, x1,y1,z2, x1,y1,z1, r,g,b,a);
        line(vc, m, x1,y2,z1, x2,y2,z1, r,g,b,a);
        line(vc, m, x2,y2,z1, x2,y2,z2, r,g,b,a);
        line(vc, m, x2,y2,z2, x1,y2,z2, r,g,b,a);
        line(vc, m, x1,y2,z2, x1,y2,z1, r,g,b,a);
        line(vc, m, x1,y1,z1, x1,y2,z1, r,g,b,a);
        line(vc, m, x2,y1,z1, x2,y2,z1, r,g,b,a);
        line(vc, m, x2,y1,z2, x2,y2,z2, r,g,b,a);
        line(vc, m, x1,y1,z2, x1,y2,z2, r,g,b,a);
    }

    private void line(VertexConsumer vc, Matrix4f m,
                      float x1,float y1,float z1, float x2,float y2,float z2,
                      float r, float g, float b, float a) {
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(1, 0, 0);
        vc.vertex(m, x2, y2, z2).color(r, g, b, a).normal(1, 0, 0);
    }
}
