package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {

    private double flySpeed = 2.0;
    private Vec3d savedPos = null;
    private float savedYaw, savedPitch;
    private boolean savedOnGround;

    public Freecam() {
        super("Freecam", "Detach camera and fly around freely while your body stays in place", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            savedPos = mc.player.getPos();
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
            savedOnGround = mc.player.isOnGround();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // keep player model in place
        mc.player.noClip = true;
        mc.player.setNoGravity(true);

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double rad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double motionX = 0, motionY = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            motionX = (-Math.sin(rad) * forward + Math.cos(rad) * strafe) * flySpeed * 0.05;
            motionZ = (Math.cos(rad) * forward + Math.sin(rad) * strafe) * flySpeed * 0.05;
            motionY = -Math.sin(pitchRad) * forward * flySpeed * 0.05;
        }

        if (mc.options.jumpKey.isPressed()) {
            motionY += flySpeed * 0.05;
        }
        if (mc.options.sneakKey.isPressed()) {
            motionY -= flySpeed * 0.05;
        }

        mc.player.setVelocity(motionX, motionY, motionZ);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && savedPos != null) {
            mc.player.setPos(savedPos.x, savedPos.y, savedPos.z);
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
            mc.player.noClip = false;
            mc.player.setNoGravity(false);
            mc.player.setVelocity(Vec3d.ZERO);
        }
        savedPos = null;
    }
}
