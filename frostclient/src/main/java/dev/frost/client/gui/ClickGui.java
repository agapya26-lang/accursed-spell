// ClickGui | Fabric 1.21.1
// minimal click GUI — one panel per category, scroll to toggle modules
package dev.frost.client.gui;

import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class ClickGui extends Screen {

    private boolean open = false;

    public ClickGui() { super(Text.literal("FrostClient")); }

    public void toggle() {
        open = !open;
        if (open) FrostClient.mc().setScreen(this);
        else if (FrostClient.mc().currentScreen == this) FrostClient.mc().setScreen(null);
    }

    // panel layout
    private static final int PANEL_W  = 100;
    private static final int PANEL_H  = 14;
    private static final int PANEL_GAP = 4;
    private static final int COLOR_BG  = 0xAA111111;
    private static final int COLOR_ON  = 0xFF4488FF;
    private static final int COLOR_OFF = 0xFF333333;
    private static final int COLOR_TXT = 0xFFFFFFFF;

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int x = 4;
        for (Module.Category cat : Module.Category.values()) {
            // category header
            ctx.fill(x, 4, x + PANEL_W, 4 + PANEL_H, 0xBB222266);
            ctx.drawTextWithShadow(textRenderer, cat.name(), x + 3, 8, COLOR_TXT);

            int y = 4 + PANEL_H + PANEL_GAP;
            for (Module m : FrostClient.modules.getByCategory(cat)) {
                int bg = m.isEnabled() ? COLOR_ON : COLOR_OFF;
                ctx.fill(x, y, x + PANEL_W, y + PANEL_H, bg);
                ctx.drawTextWithShadow(textRenderer, m.name, x + 3, y + 3, COLOR_TXT);
                y += PANEL_H + 1;
            }
            x += PANEL_W + PANEL_GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = 4;
        for (Module.Category cat : Module.Category.values()) {
            int y = 4 + PANEL_H + PANEL_GAP;
            for (Module m : FrostClient.modules.getByCategory(cat)) {
                if (mx >= x && mx <= x + PANEL_W && my >= y && my <= y + PANEL_H) {
                    m.toggle(); return true;
                }
                y += PANEL_H + 1;
            }
            x += PANEL_W + PANEL_GAP;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
