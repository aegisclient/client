package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Jesus extends Module {

    private int bobTick = 0;

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSneaking()) return;

        BlockPos below = mc.player.getBlockPos();
        FluidState fluid = mc.world.getFluidState(below);
        FluidState fluidBelow = mc.world.getFluidState(below.down());

        boolean inFluid = !fluid.isEmpty();
        boolean aboveFluid = !fluidBelow.isEmpty() && fluid.isEmpty();

        if (!inFluid && !aboveFluid) {
            bobTick = 0;
            return;
        }

        bobTick++;
        Vec3d vel = mc.player.getVelocity();

        if (mc.player.isSubmergedInWater() || mc.player.isInLava()) {
            // swim up to surface with dolphin-like motion
            double upSpeed = 0.11;
            if (bobTick % 3 == 0) {
                upSpeed = 0.09; // vary slightly to look natural
            }
            mc.player.setVelocity(vel.x, upSpeed, vel.z);
        } else if (inFluid || aboveFluid) {
            // on surface: bob up and down like a boat (NCP-compatible pattern)
            // this mimics the vanilla boat bobbing which NCP allows
            double bobY;
            if (bobTick % 4 < 2) {
                bobY = 0.015;
            } else {
                bobY = -0.005;
            }

            mc.player.setVelocity(vel.x * 0.98, bobY, vel.z * 0.98);
            mc.player.setOnGround(true);

            // send spoofed position to server at fluid surface level
            if (bobTick % 2 == 0) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                );
            }
        }
    }

    @Override
    protected void onDisable() {
        bobTick = 0;
    }
}
