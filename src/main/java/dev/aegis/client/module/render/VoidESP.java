package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class VoidESP extends Module {

    private final List<BlockPos> voidHoles = new ArrayList<>();
    private int scanDelay = 0;

    public VoidESP() {
        super("VoidESP", "Highlights holes that lead to the void", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (scanDelay > 0) {
            scanDelay--;
            return;
        }
        scanDelay = 40;

        voidHoles.clear();

        BlockPos playerPos = mc.player.getBlockPos();
        int range = 16;

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos pos = playerPos.add(x, 0, z);
                // check if there's a clear path to void (y=0 or below)
                boolean isVoid = true;
                for (int y = mc.world.getBottomY(); y < mc.world.getBottomY() + 5; y++) {
                    if (!mc.world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).isAir()) {
                        isVoid = false;
                        break;
                    }
                }
                if (isVoid) {
                    voidHoles.add(new BlockPos(pos.getX(), mc.world.getBottomY(), pos.getZ()));
                }
            }
        }
    }

    public List<BlockPos> getVoidHoles() {
        return voidHoles;
    }

    @Override
    protected void onDisable() {
        voidHoles.clear();
    }
}
