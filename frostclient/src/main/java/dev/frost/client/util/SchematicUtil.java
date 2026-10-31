// SchematicUtil | Fabric 1.21.1
// parses .litematic, .schem (Sponge), .nbt (vanilla structure)
package dev.frost.client.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class SchematicUtil {

    public record PlaceEntry(BlockPos pos, BlockState state) {}

    // load any supported format
    public static List<PlaceEntry> load(File file, BlockPos origin) {
        String name = file.getName().toLowerCase();
        try {
            if (name.endsWith(".litematic")) return loadLitematic(file, origin);
            if (name.endsWith(".schem"))     return loadSchem(file, origin);
            if (name.endsWith(".nbt"))       return loadNbt(file, origin);
        } catch (Exception ex) {
            dev.frost.client.FrostClient.LOG.error("[Schematic] load failed: {}", ex.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Sponge v2 .schem ──────────────────────────────────────────
    private static List<PlaceEntry> loadSchem(File file, BlockPos origin) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file.toPath(), NbtTagSizeTracker.ofUnlimitedBytes());
        int[] size   = root.contains("Width")
            ? new int[]{root.getShort("Width"), root.getShort("Height"), root.getShort("Length")}
            : new int[]{16, 16, 16};

        NbtList palette = root.getList("Palette", NbtElement.COMPOUND_TYPE);
        // build index→BlockState map
        Map<Integer, BlockState> paletteMap = new HashMap<>();
        // palette in schem is a compound of "blockstate_string" -> index
        NbtCompound paletteCompound = root.getCompound("Palette");
        for (String key : paletteCompound.getKeys()) {
            int idx = paletteCompound.getInt(key);
            BlockState state = parseBlockState(key);
            if (state != null) paletteMap.put(idx, state);
        }

        byte[] blockData = root.getByteArray("BlockData");
        List<PlaceEntry> entries = new ArrayList<>();
        for (int i = 0; i < blockData.length; i++) {
            int y = i / (size[0] * size[2]);
            int rem = i % (size[0] * size[2]);
            int z = rem / size[0];
            int x = rem % size[0];
            BlockState state = paletteMap.get((int) blockData[i] & 0xFF);
            if (state != null && !state.isAir()) {
                entries.add(new PlaceEntry(origin.add(x, y, z), state));
            }
        }
        return entries;
    }

    // ── Litematic ─────────────────────────────────────────────────
    private static List<PlaceEntry> loadLitematic(File file, BlockPos origin) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file.toPath(), NbtTagSizeTracker.ofUnlimitedBytes());
        NbtCompound regions = root.getCompound("Regions");
        List<PlaceEntry> entries = new ArrayList<>();

        for (String regionKey : regions.getKeys()) {
            NbtCompound region   = regions.getCompound(regionKey);
            NbtList     palette  = region.getList("BlockStatePalette", NbtElement.COMPOUND_TYPE);
            long[]      states   = region.getLongArray("BlockStates");
            NbtCompound size     = region.getCompound("Size");
            int sx = Math.abs(size.getInt("x")), sy = Math.abs(size.getInt("y")), sz = Math.abs(size.getInt("z"));
            int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));
            long mask = (1L << bits) - 1;

            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    for (int x = 0; x < sx; x++) {
                        int idx    = y * sz * sx + z * sx + x;
                        int bitOff = idx * bits;
                        int longIdx = bitOff / 64;
                        int shift   = bitOff % 64;
                        long val;
                        if (shift + bits <= 64) {
                            val = (states[longIdx] >> shift) & mask;
                        } else {
                            long lo  = states[longIdx] >>> shift;
                            long hi  = states[longIdx + 1] << (64 - shift);
                            val = (lo | hi) & mask;
                        }
                        NbtCompound entry = palette.getCompound((int) val);
                        BlockState state  = NbtHelper.toBlockState(
                            MinecraftClient.getInstance().world.createCommandRegistryWrapper(
                                net.minecraft.registry.RegistryKeys.BLOCK),
                            entry);
                        if (!state.isAir()) {
                            entries.add(new PlaceEntry(origin.add(x, y, z), state));
                        }
                    }
                }
            }
        }
        return entries;
    }

    // ── Vanilla .nbt structure ─────────────────────────────────────
    private static List<PlaceEntry> loadNbt(File file, BlockPos origin) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file.toPath(), NbtTagSizeTracker.ofUnlimitedBytes());
        NbtList palette  = root.getList("palette", NbtElement.COMPOUND_TYPE);
        NbtList blocks   = root.getList("blocks", NbtElement.COMPOUND_TYPE);
        List<PlaceEntry> entries = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            NbtCompound block = blocks.getCompound(i);
            int stateIdx = block.getInt("state");
            NbtList posList = block.getList("pos", NbtElement.INT_TYPE);
            int x = posList.getInt(0), y = posList.getInt(1), z = posList.getInt(2);
            NbtCompound stateNbt = palette.getCompound(stateIdx);
            BlockState state = NbtHelper.toBlockState(
                MinecraftClient.getInstance().world.createCommandRegistryWrapper(
                    net.minecraft.registry.RegistryKeys.BLOCK),
                stateNbt);
            if (!state.isAir()) entries.add(new PlaceEntry(origin.add(x, y, z), state));
        }
        return entries;
    }

    // parse "minecraft:stone[facing=north]" → BlockState
    private static BlockState parseBlockState(String str) {
        try {
            String id = str.contains("[") ? str.substring(0, str.indexOf('[')) : str;
            var block = Registries.BLOCK.get(Identifier.of(id));
            return block.getDefaultState();  // properties parsing omitted for brevity
        } catch (Exception e) { return null; }
    }

    // find hotbar slot holding a block matching the target blockstate
    public static int findBlock(MinecraftClient mc, BlockState target) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi) {
                if (bi.getBlock().getDefaultState().equals(target)) return i;
            }
        }
        return -1;
    }

    // export recorded blocks to .schem file
    public static void exportSchem(Map<BlockPos, String> blocks, BlockPos anchor, File out) {
        // minimal Sponge v2 schem export
        NbtCompound root = new NbtCompound();
        int minX = blocks.keySet().stream().mapToInt(BlockPos::getX).min().orElse(0);
        int minY = blocks.keySet().stream().mapToInt(BlockPos::getY).min().orElse(0);
        int minZ = blocks.keySet().stream().mapToInt(BlockPos::getZ).min().orElse(0);

        Set<String> paletteSet = new LinkedHashSet<>();
        paletteSet.add("minecraft:air");
        paletteSet.addAll(blocks.values());
        List<String> paletteList = new ArrayList<>(paletteSet);

        NbtCompound palette = new NbtCompound();
        for (int i = 0; i < paletteList.size(); i++) palette.putInt(paletteList.get(i), i);

        int sx = blocks.keySet().stream().mapToInt(p -> p.getX() - minX).max().orElse(0) + 1;
        int sy = blocks.keySet().stream().mapToInt(p -> p.getY() - minY).max().orElse(0) + 1;
        int sz = blocks.keySet().stream().mapToInt(p -> p.getZ() - minZ).max().orElse(0) + 1;

        byte[] data = new byte[sx * sy * sz];
        blocks.forEach((pos, stateId) -> {
            int x = pos.getX() - minX, y = pos.getY() - minY, z = pos.getZ() - minZ;
            int i = y * sx * sz + z * sx + x;
            data[i] = (byte) paletteList.indexOf(stateId);
        });

        root.putShort("Width",  (short) sx);
        root.putShort("Height", (short) sy);
        root.putShort("Length", (short) sz);
        root.put("Palette", palette);
        root.putByteArray("BlockData", data);
        root.putInt("Version", 2);

        try { NbtIo.writeCompressed(root, out.toPath()); }
        catch (IOException ex) { dev.frost.client.FrostClient.LOG.error("[BuildRecorder] write failed: {}", ex.getMessage()); }
    }
}
