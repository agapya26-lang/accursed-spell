// AutoTotem | Combat | Fabric 1.21.1
// keeps totem in offhand at all times, swaps from inventory if consumed
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {

    public AutoTotem() {
        super("AutoTotem", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.interactionManager == null) return;

        // if offhand already has a totem, done
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        // find totem in inventory (slots 0-35)
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                // move to offhand via creative pick or shift-click into slot 40 (offhand)
                mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    i < 9 ? i + 36 : i,  // translate hotbar index
                    40,
                    SlotActionType.SWAP,
                    mc.player
                );
                return;
            }
        }
    }
}
