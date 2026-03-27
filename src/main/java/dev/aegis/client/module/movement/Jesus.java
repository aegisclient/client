package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Jesus extends Module {

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // check if player is in or above water/lava
        BlockPos below = mc.player.getBlockPos();
        FluidState fluid = mc.world.getFluidState(below);

        if (fluid.isEmpty()) return;

        if (!mc.player.isSneaking()) {
            Vec3d vel = mc.player.getVelocity();

            // if we're at the surface or going down, push us up
            if (mc.player.isSubmergedInWater() || mc.player.isInLava()) {
                mc.player.setVelocity(vel.x, 0.11, vel.z);
            } else if (vel.y < 0) {
                // hovering on surface
                mc.player.setVelocity(vel.x, 0.0, vel.z);
                mc.player.setOnGround(true);
            }
        }
    }
}
