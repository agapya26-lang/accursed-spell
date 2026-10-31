// CombatUtil | Shared combat math | Fabric 1.21.1
package dev.frost.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.Comparator;
import java.util.Optional;

public final class CombatUtil {

    private CombatUtil() {}

    // nearest player excluding self within range
    public static PlayerEntity nearestPlayer(MinecraftClient mc, float range) {
        if (mc.player == null || mc.world == null) return null;
        return mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .filter(p -> mc.player.distanceTo(p) <= range)
            .min(Comparator.comparingDouble(mc.player::distanceTo))
            .orElse(null);
    }

    // nearest player inside FOV cone
    public static PlayerEntity nearestPlayerInFov(MinecraftClient mc, float range, float fovDeg) {
        if (mc.player == null || mc.world == null) return null;
        return mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .filter(p -> mc.player.distanceTo(p) <= range)
            .filter(p -> angleTo(mc, p) <= fovDeg / 2f)
            .min(Comparator.comparingDouble(mc.player::distanceTo))
            .orElse(null);
    }

    // angle in degrees between player look and target
    public static float angleTo(MinecraftClient mc, Entity target) {
        Vec3d diff = target.getPos().subtract(mc.player.getEyePos()).normalize();
        Vec3d look = mc.player.getRotationVec(1f);
        double dot = look.dot(diff);
        return (float) Math.toDegrees(Math.acos(Math.min(1.0, dot)));
    }

    // yaw/pitch toward target position
    public static float[] getAnglesTo(PlayerEntity self, Vec3d target) {
        Vec3d eye  = self.getEyePos();
        double dx  = target.x - eye.x;
        double dy  = target.y - eye.y;
        double dz  = target.z - eye.z;
        double h   = Math.sqrt(dx * dx + dz * dz);
        float  yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch = (float)(-Math.toDegrees(Math.atan2(dy, h)));
        return new float[]{yaw, pitch};
    }

    // delta between two angles (wraps -180..180)
    public static float deltaAngle(float from, float to) {
        float d = ((to - from) % 360 + 540) % 360 - 180;
        return d;
    }

    // set client rotation; silent=true means only update the network packet fields
    public static void setRotation(MinecraftClient mc, float yaw, float pitch, boolean silent) {
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.prevYaw   = yaw;
        mc.player.prevPitch = pitch;
    }

    // face entity (silent aim style)
    public static void faceEntity(MinecraftClient mc, Entity target, boolean silent) {
        Vec3d aimAt = target.getPos().add(0, target.getHeight() * 0.8, 0);
        float[] angs = getAnglesTo(mc.player, aimAt);
        setRotation(mc, angs[0], angs[1], silent);
    }

    // simplified crystal explosion damage estimate
    public static float crystalDamage(World world, Vec3d pos, LivingEntity entity) {
        return explosionDamage(world, pos, entity, 6f);
    }

    // generic explosion damage approximation (no actual explosion created)
    public static float explosionDamage(World world, Vec3d center, LivingEntity entity, float power) {
        double dist = entity.getPos().distanceTo(center);
        if (dist > power * 2) return 0;
        double exposure = 1.0 - (dist / (power * 2));
        double dmg      = (exposure * exposure + exposure) / 2.0 * power * 7.0;
        // rough resistance reduction
        float resistance = entity.getAttributeValue(
            net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR) * 0.04f;
        dmg *= (1f - Math.min(20f, resistance) / 25f);
        return (float) dmg;
    }

    // check if a block can hold a crystal (obsidian or bedrock)
    public static boolean isValidCrystalBase(World world, BlockPos pos) {
        var state = world.getBlockState(pos);
        var above = world.getBlockState(pos.up());
        var above2 = world.getBlockState(pos.up(2));
        return (state.isOf(net.minecraft.block.Blocks.OBSIDIAN) || state.isOf(net.minecraft.block.Blocks.BEDROCK))
            && above.isAir() && above2.isAir();
    }

    // find item in hotbar (slots 0-8), returns slot index or -1
    public static int findInHotbar(MinecraftClient mc, Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    // find a splash health potion in hotbar
    public static int findSplashHealthPot(MinecraftClient mc) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.SPLASH_POTION)) {
                // check if it contains health effect — simplified check by name/tag
                var potion = net.minecraft.component.DataComponentTypes.POTION_CONTENTS;
                var contents = stack.get(potion);
                if (contents != null && contents.potion().isPresent()) {
                    var p = contents.potion().get().value();
                    if (p == Potions.HEALING.value() || p == Potions.STRONG_HEALING.value()) return i;
                }
            }
        }
        return -1;
    }
}
