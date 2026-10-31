// ModuleManager — central registry
package dev.frost.client.modules;

import dev.frost.client.modules.combat.*;
import dev.frost.client.modules.automation.*;
import dev.frost.client.modules.visuals.*;
import dev.frost.client.modules.security.*;

import java.util.*;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // ── COMBAT ──
        register(new AutoCrystal());
        register(new AnchorMacro());
        register(new MaceAutomation());
        register(new AimAssist());
        register(new SilentAim());
        register(new TriggerBot());
        register(new AutoTotem());
        register(new AutoPot());
        register(new AutoRefill());

        // ── AUTOMATION ──
        register(new AutoSchematic());
        register(new BuildRecorder());
        register(new FastPlace());
        register(new FastBreak());
        register(new AutoAreaMiner());

        // ── VISUALS ──
        register(new PlayerESP());
        register(new PearlPredictor());
        register(new NetheriteFinderModule());
        register(new ChunkFinderModule());

        // ── SECURITY ──
        register(new PanicModule());
        register(new SelfDestruct());
        register(new ConfigProfiles());
    }

    private void register(Module m) { modules.add(m); }

    public List<Module> getAll()                       { return modules; }
    public List<Module> getByCategory(Module.Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }

    public Optional<Module> get(String name) {
        return modules.stream().filter(m -> m.name.equalsIgnoreCase(name)).findFirst();
    }

    public void panicDisableAll() {
        modules.forEach(Module::forceDisable);
    }
}
