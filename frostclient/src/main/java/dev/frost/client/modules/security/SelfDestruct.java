// SelfDestruct | Security | Fabric 1.21.1
// unloads all frost classes and deletes jar / config from disk
package dev.frost.client.modules.security;

import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.*;

public class SelfDestruct extends Module {

    public SelfDestruct() { super("SelfDestruct", Category.SECURITY); }

    @Override
    protected void onEnable() {
        FrostClient.LOG.warn("[Frost] SELF-DESTRUCT initiated");
        FrostClient.modules.panicDisableAll();

        // delete config directory
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("frostclient");
        deleteDir(configDir.toFile());

        // attempt to delete own jar from mods folder
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, "frostclient*.jar")) {
            for (Path p : stream) {
                p.toFile().deleteOnExit();   // can't delete loaded jar on Windows, schedule on exit
                FrostClient.LOG.warn("[Frost] scheduled deletion: {}", p);
            }
        } catch (Exception ex) {
            FrostClient.LOG.error("[Frost] jar delete failed: {}", ex.getMessage());
        }

        // null out static references
        FrostClient.modules  = null;
        FrostClient.configs  = null;
        FrostClient.clickGui = null;
        enabled = false;
    }

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDir(f); else f.delete();
        }
        dir.delete();
    }
}
