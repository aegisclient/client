package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class Nuker extends Module {

    private int radius = 4;

    public Nuker() {
        super("Nuker", "Destroys blocks around you in a radius", Category.WORLD, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int blocksPerTick = 2; // limit to avoid lag
        int broken = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = -radius; z <= radius; z++) {
                    if (broken >= blocksPerTick) return;

                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue; // skip bedrock etc

                    // check reach distance
                    double dist = mc.player.getPos().distanceTo(pos.toCenterPos());
                    if (dist > 4.5) continue;

                    mc.interactionManager.attackBlock(pos, Direction.UP);
                    mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                    broken++;
                }
            }
        }
    }

    public void setRadius(int radius) { this.radius = radius; }
}
