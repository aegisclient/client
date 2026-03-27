package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class AutoGap extends Module {

    private double healthThreshold = 14.0;
    private boolean eating = false;
    private int prevSlot = -1;

    public AutoGap() {
        super("AutoGap", "Automatically eats golden apples when health drops below threshold", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        if (health <= healthThreshold && !eating) {
            // find golden apple or enchanted golden apple
            int gapSlot = findGoldenApple();
            if (gapSlot == -1) return;

            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = gapSlot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            eating = true;
        }

        if (eating) {
            if (!mc.player.isUsingItem()) {
                // done eating or interrupted
                if (prevSlot != -1) {
                    mc.player.getInventory().selectedSlot = prevSlot;
                    prevSlot = -1;
                }
                eating = false;
            }
        }
    }

    private int findGoldenApple() {
        // prefer enchanted golden apples
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                return i;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.GOLDEN_APPLE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        if (eating && prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
        eating = false;
        prevSlot = -1;
    }
}
