// BuildRecorder | Automation | Fabric 1.21.1
// records manual block placements → exportable schematic
package dev.frost.client.modules.automation;

import dev.frost.client.modules.Module;
import dev.frost.client.util.SchematicUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;

public class BuildRecorder extends Module {

    public File   outputFile = new File("frost_recorded.schem");

    // recorded as blockpos → blockstate string
    private final Map<BlockPos, String> recorded = new LinkedHashMap<>();
    private BlockPos anchorPos = null;

    public BuildRecorder() {
        super("BuildRecorder", Category.AUTOMATION);
        // actual capture is done via mixin intercepting block place packets
    }

    @Override
    protected void onEnable()  {
        recorded.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) anchorPos = mc.player.getBlockPos();
    }

    @Override
    protected void onDisable() {
        if (recorded.isEmpty()) return;
        SchematicUtil.exportSchem(recorded, anchorPos, outputFile);
        dev.frost.client.FrostClient.LOG.info("[BuildRecorder] saved {} blocks to {}", recorded.size(), outputFile);
    }

    // called from mixin when a block is placed
    public void record(BlockPos pos, String stateId) {
        if (isEnabled()) recorded.put(pos.toImmutable(), stateId);
    }

    public boolean isRecording() { return isEnabled(); }
}
