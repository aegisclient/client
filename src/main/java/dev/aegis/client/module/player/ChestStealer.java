package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class ChestStealer extends Module {

    private int tickDelay = 0;
    private int delayBetweenItems = 1;

    public ChestStealer() {
        super("ChestStealer", "Automatically takes all items from chests", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen containerScreen)) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        var handler = containerScreen.getScreenHandler();
        int containerSlots = handler.getRows() * 9;

        for (int i = 0; i < containerSlots; i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                // shift-click to move item to player inventory
                mc.interactionManager.clickSlot(
                        handler.syncId,
                        i,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );
                tickDelay = delayBetweenItems;
                return; // one item at a time to look more natural
            }
        }

        // all items taken, close the chest
        mc.player.closeHandledScreen();
    }
}
