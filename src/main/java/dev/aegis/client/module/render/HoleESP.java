package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module {

    private int scanRange = 8;
    private final List<BlockPos> safeHoles = new ArrayList<>();
    private final List<BlockPos> unsafeHoles = new ArrayList<>();
    private int scanDelay = 0;

    public HoleESP() {
        super("HoleESP", "Highlights safe holes for crystal PvP (bedrock and obsidian holes)", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // don't scan every tick
        if (scanDelay > 0) {
            scanDelay--;
            return;
        }
        scanDelay = 5;

        safeHoles.clear();
        unsafeHoles.clear();

        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -scanRange; z <= scanRange; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;

                    HoleType type = getHoleType(pos);
                    if (type == HoleType.BEDROCK) {
                        safeHoles.add(pos);
                    } else if (type == HoleType.OBSIDIAN || type == HoleType.MIXED) {
                        unsafeHoles.add(pos);
                    }
                }
            }
        }
    }

    private HoleType getHoleType(BlockPos pos) {
        boolean allBedrock = true;
        boolean allBlast = true;

        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

        for (Direction dir : sides) {
            BlockState state = mc.world.getBlockState(pos.offset(dir));

            if (state.getBlock() != Blocks.BEDROCK) {
                allBedrock = false;
            }

            boolean isBlastProof = state.getBlock() == Blocks.BEDROCK
                    || state.getBlock() == Blocks.OBSIDIAN
                    || state.getBlock() == Blocks.CRYING_OBSIDIAN
                    || state.getBlock() == Blocks.NETHERITE_BLOCK
                    || state.getBlock() == Blocks.ENDER_CHEST
                    || state.getBlock() == Blocks.ANVIL;

            if (!isBlastProof) {
                allBlast = false;
            }
        }

        if (allBedrock) return HoleType.BEDROCK;
        if (allBlast) return allBedrock ? HoleType.BEDROCK : HoleType.OBSIDIAN;
        return HoleType.NONE;
    }

    public List<BlockPos> getSafeHoles() { return safeHoles; }
    public List<BlockPos> getUnsafeHoles() { return unsafeHoles; }

    private enum HoleType { NONE, BEDROCK, OBSIDIAN, MIXED }
}
