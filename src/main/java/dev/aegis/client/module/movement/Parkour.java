package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class Parkour extends Module {

    public Parkour() {
        super("Parkour", "Automatically jumps at the edge of blocks for parkour", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        // check if the block below ahead is air (edge detection)
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // check if there's air below in the movement direction
        BlockPos below = new BlockPos((int) Math.floor(x), (int) (y - 1), (int) Math.floor(z));
        boolean onEdge = mc.world.getBlockState(below).isAir();

        if (onEdge && mc.player.input.movementForward > 0) {
            mc.player.jump();
        }
    }
}
