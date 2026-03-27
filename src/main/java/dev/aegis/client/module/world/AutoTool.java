package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoTool extends Module {

    public AutoTool() {
        super("AutoTool", "Automatically switches to the best tool for the block you're mining", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        if (mc.crosshairTarget instanceof BlockHitResult blockHit) {
            if (blockHit.getType() != HitResult.Type.BLOCK) return;

            BlockState state = mc.world.getBlockState(blockHit.getBlockPos());

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                float speed = stack.getMiningSpeedMultiplier(state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                mc.player.getInventory().selectedSlot = bestSlot;
            }
        }
    }
}
