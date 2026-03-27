package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class HoleFiller extends Module {

    private int range = 4;
    private int delay = 0;

    public HoleFiller() {
        super("HoleFiller", "Automatically fills 1x1 holes near you with obsidian", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int obsidianSlot = findObsidianSlot();
        if (obsidianSlot == -1) return;

        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos pos = playerPos.add(x, -1, z);

                if (!isHole(pos)) continue;

                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = obsidianSlot;

                BlockHitResult hitResult = new BlockHitResult(
                        Vec3d.ofCenter(pos),
                        Direction.UP,
                        pos,
                        false
                );
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

                mc.player.getInventory().selectedSlot = prevSlot;
                delay = 2;
                return;
            }
        }
    }

    private boolean isHole(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isAir()) return false;

        // check all 4 sides and bottom are solid
        return !mc.world.getBlockState(pos.north()).isAir()
                && !mc.world.getBlockState(pos.south()).isAir()
                && !mc.world.getBlockState(pos.east()).isAir()
                && !mc.world.getBlockState(pos.west()).isAir()
                && !mc.world.getBlockState(pos.down()).isAir();
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.OBSIDIAN) {
                return i;
            }
        }
        return -1;
    }
}
