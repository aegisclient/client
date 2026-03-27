package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class Replenish extends Module {

    private int threshold = 16;
    private int delay = 0;

    public Replenish() {
        super("Replenish", "Automatically refills hotbar stacks from inventory", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(hotbarSlot);
            if (hotbarStack.isEmpty()) continue;
            if (!hotbarStack.isStackable()) continue;
            if (hotbarStack.getCount() >= threshold) continue;

            // find same item in inventory
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = mc.player.getInventory().getStack(invSlot);
                if (invStack.isEmpty()) continue;
                if (!ItemStack.canCombine(hotbarStack, invStack)) continue;

                // quick-move the inventory stack to hotbar
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        invSlot, hotbarSlot, SlotActionType.SWAP, mc.player
                );
                delay = 2;
                return;
            }
        }
    }
}
