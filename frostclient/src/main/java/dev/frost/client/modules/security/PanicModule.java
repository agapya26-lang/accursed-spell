// PanicModule | Security | Fabric 1.21.1
// disables all modules instantly — triggered by keybind in FrostClient.java
package dev.frost.client.modules.security;

import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;

public class PanicModule extends Module {

    public PanicModule() { super("PanicModule", Category.SECURITY); }

    @Override
    protected void onEnable() {
        FrostClient.modules.panicDisableAll();
        FrostClient.LOG.info("[Frost] PANIC — all modules disabled");
        // disable self too
        enabled = false;
    }
}
