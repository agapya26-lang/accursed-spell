// AutoRefill | Combat | Fabric 1.21.1
// watches hotbar slots and pulls replacements from inventory
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

public class AutoRefill extends Module {

    public AutoRefill() {
        super("AutoRefill", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.interactionManager == null) return;

        var inv = mc.player.getInventory();
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = inv.getStack(hotbarSlot);
            if (stack.isEmpty()) continue;

            boolean needRefill = false;
            Item item = stack.getItem();

            // stackable consumables
            if (stack.getMaxCount() > 1 && stack.getCount() == 1) needRefill = true;
            // end crystals
            if (item == Items.END_CRYSTAL && stack.getCount() <= 4) needRefill = true;
            // arrows
            if (item == Items.ARROW && stack.getCount() <= 8) needRefill = true;

            if (!needRefill) continue;

            // find matching item in main inventory (slots 9-35)
            for (int i = 9; i < 36; i++) {
                ItemStack candidate = inv.getStack(i);
                if (candidate.isOf(item)) {
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i,
                        hotbarSlot,
                        SlotActionType.SWAP,
                        mc.player
                    );
                    break;
                }
            }
        }
    }
}
