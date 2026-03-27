package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class InventoryCleaner extends Module {

    private int delay = 0;

    public InventoryCleaner() {
        super("InventoryCleaner", "Automatically drops useless items from your inventory", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (isJunk(stack)) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i, 1, SlotActionType.THROW, mc.player
                );
                delay = 3;
                return;
            }
        }
    }

    private boolean isJunk(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        return name.contains("rotten_flesh")
                || name.contains("poisonous_potato")
                || name.contains("spider_eye")
                || name.contains("pufferfish")
                || name.contains("dead_bush");
    }
}
