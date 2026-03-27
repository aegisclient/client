package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Surround extends Module {

    private int delay = 0;
    private boolean center = true;

    public Surround() {
        super("Surround", "Places obsidian around your feet for crystal PvP protection", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null && center) {
            // center on block
            double x = Math.floor(mc.player.getX()) + 0.5;
            double z = Math.floor(mc.player.getZ()) + 0.5;
            mc.player.setPosition(x, mc.player.getY(), z);
        }
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

        BlockPos feet = mc.player.getBlockPos();
        BlockPos[] surroundPositions = {
                feet.north(), feet.south(), feet.east(), feet.west(),
                feet.north().down(), feet.south().down(), feet.east().down(), feet.west().down()
        };

        for (BlockPos pos : surroundPositions) {
            if (!mc.world.getBlockState(pos).isAir()) continue;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = obsidianSlot;

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3d.ofCenter(pos),
                    Direction.UP,
                    pos.down(),
                    false
            );
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.getInventory().selectedSlot = prevSlot;
            delay = 1;
            return;
        }
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
