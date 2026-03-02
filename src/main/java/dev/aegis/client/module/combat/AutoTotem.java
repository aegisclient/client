package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends Module {

    public AutoTotem() {
        super("AutoTotem", "Automatically puts a totem of undying in your offhand", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return; // don't mess with inventory while in a screen

        // check if offhand already has totem
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // find totem in inventory
        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) return;

        // convert player inventory slot to screen handler slot
        int screenSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;

        // pick up totem
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                screenSlot,
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        // put it in offhand (slot 45)
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                45,
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        // put whatever was in offhand back
        if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    screenSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );
        }
    }
}
