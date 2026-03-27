package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class BreakHighlight extends Module {

    public BreakHighlight() {
        super("BreakHighlight", "Shows block breaking progress with colored highlight overlay", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // rendering is handled by the mixin/render event
        // this module just acts as a toggle
    }

    public static BlockPos getTargetBlock() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        }
        return null;
    }
}
