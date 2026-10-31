// FrostClient v1 | Fabric 1.21.1 | MSVC-style, client-side only
package dev.frost.client;

import dev.frost.client.config.ConfigManager;
import dev.frost.client.event.EventBus;
import dev.frost.client.gui.ClickGui;
import dev.frost.client.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrostClient implements ClientModInitializer {

    public static final String NAME    = "FrostClient";
    public static final String VERSION = "v1";
    public static final Logger LOG     = LoggerFactory.getLogger(NAME);

    public static FrostClient    INSTANCE;
    public static ModuleManager  modules;
    public static ConfigManager  configs;
    public static EventBus       eventBus;
    public static ClickGui       clickGui;

    // keybinds
    private static KeyBinding kb_gui;
    private static KeyBinding kb_panic;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOG.info("[Frost] booting {} {}", NAME, VERSION);

        eventBus = new EventBus();
        modules  = new ModuleManager();
        configs  = new ConfigManager();
        clickGui = new ClickGui();

        kb_gui   = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.frostclient.gui",   InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "FrostClient"));
        kb_panic = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.frostclient.panic", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_END,          "FrostClient"));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        configs.load("default");

        LOG.info("[Frost] ready — {} modules loaded", modules.getAll().size());
    }

    private void onTick(MinecraftClient mc) {
        if (kb_gui.wasPressed())   clickGui.toggle();
        if (kb_panic.wasPressed()) modules.panicDisableAll();
        eventBus.fireTick(mc);
    }

    public static MinecraftClient mc() { return MinecraftClient.getInstance(); }
}
