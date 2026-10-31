// ConfigManager | Fabric 1.21.1
// saves module enabled states to .json profiles in config/frostclient/
package dev.frost.client.config;

import com.google.gson.*;
import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ConfigManager {

    private final Path configDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve("frostclient");
        try { Files.createDirectories(configDir); }
        catch (IOException e) { FrostClient.LOG.error("[Config] mkdir failed: {}", e.getMessage()); }
    }

    public void save(String profile) {
        JsonObject root = new JsonObject();
        for (Module m : FrostClient.modules.getAll()) {
            root.addProperty(m.name, m.isEnabled());
        }
        Path file = configDir.resolve(profile + ".json");
        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(root, w);
        } catch (IOException e) {
            FrostClient.LOG.error("[Config] save failed: {}", e.getMessage());
        }
    }

    public void load(String profile) {
        Path file = configDir.resolve(profile + ".json");
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            JsonObject root = gson.fromJson(r, JsonObject.class);
            for (Module m : FrostClient.modules.getAll()) {
                if (root.has(m.name)) {
                    boolean on = root.get(m.name).getAsBoolean();
                    if (on != m.isEnabled()) m.toggle();
                }
            }
        } catch (IOException e) {
            FrostClient.LOG.error("[Config] load failed: {}", e.getMessage());
        }
    }
}
