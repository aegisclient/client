package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends Module {

    public FastPlace() {
        super("FastPlace", "Removes the delay between block placements", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // bypass the 4-tick place delay by continuously attempting to place
        if (mc.options.useKey.isPressed()) {
            HitResult hit = mc.crosshairTarget;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
            }
        }
    }
}
