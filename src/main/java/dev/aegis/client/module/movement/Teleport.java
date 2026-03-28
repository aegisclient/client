package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Teleport extends Module {

    private double maxDistance = 100.0;
    private boolean ready = false;

    public Teleport() {
        super("Teleport", "Keybind: T | Teleports where you look (100 blocks)", Category.MOVEMENT, GLFW.GLFW_KEY_T);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        HitResult hit = mc.player.raycast(maxDistance, 1.0f, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            Vec3d target = new Vec3d(
                    blockHit.getBlockPos().getX() + 0.5,
                    blockHit.getBlockPos().getY() + 1.0,
                    blockHit.getBlockPos().getZ() + 0.5
            );

            Vec3d start = mc.player.getPos();
            double dist = start.distanceTo(target);

            // Send position packets in steps to avoid rubberband
            int steps = Math.max(2, (int) Math.ceil(dist / 8.0));

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
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(target.x, target.y, target.z, true)
            );

            ChatHelper.info("Teleported " + String.format("%.1f", dist) + " blocks");
        } else {
            // No block hit — teleport forward in look direction
            Vec3d look = mc.player.getRotationVec(1.0f);
            Vec3d target = mc.player.getEyePos().add(look.multiply(maxDistance));
            Vec3d start = mc.player.getPos();
            double dist = start.distanceTo(target);
            int steps = Math.max(2, (int) Math.ceil(dist / 8.0));

            for (int i = 1; i <= steps; i++) {
                double t2 = (double) i / steps;
                double x = start.x + (target.x - start.x) * t2;
                double y = start.y + (target.y - start.y) * t2;
                double z = start.z + (target.z - start.z) * t2;
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                );
            }

            mc.player.setPosition(target.x, target.y, target.z);
            ChatHelper.info("Teleported forward " + String.format("%.1f", dist) + " blocks");
        }

        // Auto-disable after teleporting (it's an instant action, not a toggle)
        disable();
    }
}
