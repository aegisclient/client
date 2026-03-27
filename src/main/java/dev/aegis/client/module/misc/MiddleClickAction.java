package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class MiddleClickAction extends Module {

    private boolean wasPressing = false;

    public MiddleClickAction() {
        super("MiddleClickAction", "Middle click to add friend or throw pearl at target", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        long window = mc.getWindow().getHandle();
        boolean pressing = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (pressing && !wasPressing) {
            if (mc.targetedEntity instanceof PlayerEntity target) {
                // throw pearl at target if holding one
                int pearlSlot = findPearlSlot();
                if (pearlSlot != -1) {
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = pearlSlot;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prev;
                }
            }
        }

        wasPressing = pressing;
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
}
