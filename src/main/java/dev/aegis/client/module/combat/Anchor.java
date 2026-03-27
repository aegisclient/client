package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class Anchor extends Module {

    private double range = 5.0;
    private int delay = 2;
    private int ticks = 0;

    public Anchor() {
        super("Anchor", "Automatically places and detonates respawn anchors for PvP", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        // anchors only explode outside the nether
        if (mc.world.getDimension().respawnAnchorWorks()) return;

        if (ticks > 0) {
            ticks--;
            return;
        }

        // find nearest enemy
        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.isAlive())
                .filter(p -> mc.player.distanceTo(p) <= range + 2)
                .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
                .orElse(null);

        if (target == null) return;

        // look for existing charged anchor nearby to detonate
        BlockPos targetPos = target.getBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                        // detonate it by right clicking
                        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        ticks = delay;
                        return;
                    }
                }
            }
        }

        // place a new anchor if we have glowstone and anchors
        int anchorSlot = findItem(Items.RESPAWN_ANCHOR);
        int glowSlot = findItem(Items.GLOWSTONE);
        if (anchorSlot == -1 || glowSlot == -1) return;

        // place near target
        BlockPos placePos = findPlacePos(target);
        if (placePos == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;

        // place anchor
        mc.player.getInventory().selectedSlot = anchorSlot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(placePos), Direction.UP, placePos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        // charge with glowstone
        mc.player.getInventory().selectedSlot = glowSlot;
        hit = new BlockHitResult(Vec3d.ofCenter(placePos), Direction.UP, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        mc.player.getInventory().selectedSlot = prevSlot;
        ticks = delay;
    }

    private BlockPos findPlacePos(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        for (Direction dir : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos pos = targetPos.offset(dir);
            if (mc.world.getBlockState(pos).isAir() && !mc.world.getBlockState(pos.down()).isAir()) {
                if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) <= range) {
                    return pos;
                }
            }
        }
        return null;
    }

    private int findItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }
}
