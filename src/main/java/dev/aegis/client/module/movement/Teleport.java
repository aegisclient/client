package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Teleport extends Module {

    private double maxDistance = 30.0;

    public Teleport() {
        super("Teleport", "Teleport to where you're looking by right-clicking", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (mc.options.useKey.wasPressed()) {
            HitResult hit = mc.player.raycast(maxDistance, 1.0f, false);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                Vec3d target = Vec3d.ofCenter(blockHit.getBlockPos().up());

                // send position packets along path to avoid rubberband
                Vec3d start = mc.player.getPos();
                double dist = start.distanceTo(target);
                int steps = (int) Math.ceil(dist / 5.0);

                for (int i = 1; i <= steps; i++) {
                    double t = (double) i / steps;
                    double x = start.x + (target.x - start.x) * t;
                    double y = start.y + (target.y - start.y) * t;
                    double z = start.z + (target.z - start.z) * t;
                    mc.player.networkHandler.sendPacket(
                            new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                    );
                }

                mc.player.setPosition(target.x, target.y, target.z);
            }
        }
    }
}
