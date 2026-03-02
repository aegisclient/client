package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", "Automatically places blocks beneath you", Category.PLAYER, GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos below = mc.player.getBlockPos().down();

        // only place if the block below is air
        if (!mc.world.getBlockState(below).isAir()) return;

        // find a block to place in hotbar
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        // try to find a face to place against
        Direction placeDir = getPlaceDirection(below);
        if (placeDir == null) return;

        BlockPos placeAgainst = below.offset(placeDir);
        Direction placeSide = placeDir.getOpposite();

        Vec3d hitVec = Vec3d.ofCenter(placeAgainst).add(
                placeSide.getOffsetX() * 0.5,
                placeSide.getOffsetY() * 0.5,
                placeSide.getOffsetZ() * 0.5
        );

        BlockHitResult hitResult = new BlockHitResult(hitVec, placeSide, placeAgainst, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                // dont place valuable blocks
                if (block == Blocks.DIAMOND_BLOCK || block == Blocks.EMERALD_BLOCK) continue;
                return i;
            }
        }
        return -1;
    }

    private Direction getPlaceDirection(BlockPos pos) {
        // check each direction for a solid block to place against
        for (Direction dir : Direction.values()) {
            BlockState neighbor = mc.world.getBlockState(pos.offset(dir));
            if (!neighbor.isAir()) {
                return dir;
            }
        }
        return null;
    }
}
