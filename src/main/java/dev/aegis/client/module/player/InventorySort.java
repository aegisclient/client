package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class InventorySort extends Module {

    private boolean sorted = false;
    private int sortStep = 0;
    private int delay = 0;

    public InventorySort() {
        super("InventorySort", "Sorts your inventory by item type on toggle", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        sorted = false;
        sortStep = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return; // dont mess with open GUIs
        if (sorted) return;

        if (delay > 0) {
            delay--;
            return;
        }

        // simple sort: move weapons to first slots, then tools, then blocks, then misc
        // do one swap per tick to avoid getting kicked
        for (int i = 9; i < 36; i++) {
            for (int j = i + 1; j < 36; j++) {
                ItemStack stackI = mc.player.getInventory().getStack(i);
                ItemStack stackJ = mc.player.getInventory().getStack(j);

                if (getSortPriority(stackJ) < getSortPriority(stackI)) {
                    // swap via click
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId, j, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                    delay = 1;
                    return;
                }
            }
        }

        sorted = true;
        this.disable();
    }

    private int getSortPriority(ItemStack stack) {
        if (stack.isEmpty()) return 100;
        Item item = stack.getItem();
        if (item instanceof SwordItem) return 0;
        if (item instanceof AxeItem) return 1;
        if (item instanceof BowItem || item instanceof CrossbowItem) return 2;
        if (item instanceof PickaxeItem) return 3;
        if (item instanceof ShovelItem) return 4;
        if (item instanceof ArmorItem) return 5;
        if (item instanceof BlockItem) return 6;
        if (item.isFood()) return 7;
        return 50;
    }
}
