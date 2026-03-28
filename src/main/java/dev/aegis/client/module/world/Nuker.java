package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class Nuker extends Module {

    private int radius = 4;
    private BlockPos currentTarget = null;

    public Nuker() {
        super("Nuker", "Destroys blocks around you in a radius", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // continue breaking current target if still valid
        if (currentTarget != null) {
            BlockState state = mc.world.getBlockState(currentTarget);
            if (!state.isAir() && state.getHardness(mc.world, currentTarget) >= 0) {
                double dist = mc.player.getPos().distanceTo(currentTarget.toCenterPos());
                if (dist <= 4.5) {
                    mc.interactionManager.updateBlockBreakingProgress(currentTarget, Direction.UP);
                    return;
                }
            }
            // target is broken or out of range, find next
            currentTarget = null;
        }

        // find next block to break
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue;

                    double dist = mc.player.getPos().distanceTo(pos.toCenterPos());
                    if (dist > 4.5) continue;

                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos;
                    }
                }
            }
        }

        if (best != null) {
            currentTarget = best;
            mc.interactionManager.attackBlock(best, Direction.UP);
        }
    }

    @Override
    protected void onDisable() {
        currentTarget = null;
    }

    public void setRadius(int radius) { this.radius = radius; }
}
