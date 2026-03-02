package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class AutoMine extends Module {

    private BlockPos currentTarget = null;
    private boolean mining = false;

    public AutoMine() {
        super("AutoMine", "Automatically mines the block you're looking at", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            if (mining) {
                mining = false;
                currentTarget = null;
            }
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        Direction side = blockHit.getSide();

        if (!pos.equals(currentTarget)) {
            currentTarget = pos;
            mining = true;
        }

        if (mining) {
            mc.interactionManager.updateBlockBreakingProgress(pos, side);
        }
    }

    @Override
    protected void onDisable() {
        mining = false;
        currentTarget = null;
    }
}
