package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AutoFarm extends Module {

    private int range = 4;
    private int delay = 0;

    public AutoFarm() {
        super("AutoFarm", "Automatically harvests and replants mature crops", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof CropBlock crop) {
                        if (crop.isMature(state)) {
                            mc.interactionManager.attackBlock(pos, Direction.UP);
                            delay = 2;
                            return;
                        }
                    }
                }
            }
        }
    }
}
