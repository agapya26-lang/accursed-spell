// ConfigProfiles | Security | Fabric 1.21.1
// saves / loads named module state profiles
package dev.frost.client.modules.security;

import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;

public class ConfigProfiles extends Module {

    public String activeProfile = "default";

    public ConfigProfiles() { super("ConfigProfiles", Category.SECURITY); }

    public void save(String name) {
        FrostClient.configs.save(name);
        FrostClient.LOG.info("[Frost] config saved: {}", name);
    }

    public void load(String name) {
        FrostClient.configs.load(name);
        activeProfile = name;
        FrostClient.LOG.info("[Frost] config loaded: {}", name);
    }
}
