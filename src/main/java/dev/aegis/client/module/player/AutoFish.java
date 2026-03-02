package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class AutoFish extends Module {

    private boolean waitingForBite = false;
    private int recastDelay = 0;

    public AutoFish() {
        super("AutoFish", "Automatically reels in and recasts your fishing rod", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // make sure we're holding a fishing rod
        if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD) return;

        if (recastDelay > 0) {
            recastDelay--;
            if (recastDelay == 0) {
                // cast the rod
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                waitingForBite = true;
            }
            return;
        }

        // check if the bobber caught something
        if (mc.player.fishHook != null) {
            if (mc.player.fishHook.isInOpenWater()) {
                // check if bobber went under water (caught something)
                if (mc.player.fishHook.getVelocity().y < -0.05 && waitingForBite) {
                    // reel in
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    waitingForBite = false;
                    recastDelay = 20; // wait 1 second before recasting
                }
            }
        } else if (!waitingForBite) {
            // no bobber out, cast the rod
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            waitingForBite = true;
        }
    }

    @Override
    protected void onDisable() {
        waitingForBite = false;
        recastDelay = 0;
    }
}
