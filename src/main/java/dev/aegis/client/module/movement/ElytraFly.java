package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ElytraFly extends Module {

    // 2b2t elytra speed limit is roughly 150km/h (~2.08 blocks/tick)
    // we stay well under that to avoid flags
    private double maxSpeed = 1.6;
    private double accel = 0.04;
    private boolean autoOpen = true;
    private int jumpTicks = 0;
    private double currentSpeed = 0;

    public ElytraFly() {
        super("ElytraFly", "Fly with elytra at controlled speed, auto-opens on jump", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        currentSpeed = 0;
        jumpTicks = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // check if wearing elytra
        if (mc.player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA) return;

        // auto open elytra when falling
        if (autoOpen && !mc.player.isFallFlying() && !mc.player.isOnGround()) {
            if (jumpTicks > 0) {
                jumpTicks--;
            }
            if (mc.player.getVelocity().y < -0.1 && jumpTicks == 0) {
                mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
                );
            }
        }

        if (mc.options.jumpKey.wasPressed()) {
            jumpTicks = 10;
        }

        if (!mc.player.isFallFlying()) {
            currentSpeed = 0;
            return;
        }

        // controlled flight with gradual acceleration (looks natural to anti-cheat)
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        float pitch = (float) Math.toRadians(mc.player.getPitch());

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double motionX = 0, motionY = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            // gradually accelerate instead of instant max speed
            currentSpeed = Math.min(currentSpeed + accel, maxSpeed);

            double cos = Math.cos(yaw);
            double sin = Math.sin(yaw);
            double speedFactor = currentSpeed * 0.05;
            motionX = (-sin * forward + cos * strafe) * speedFactor;
            motionZ = (cos * forward + sin * strafe) * speedFactor;
            motionY = -Math.sin(pitch) * forward * speedFactor;

            // clamp vertical to avoid detection
            motionY = Math.max(-0.08, Math.min(0.08, motionY));
        } else {
            // decelerate naturally when not pressing keys
            currentSpeed *= 0.92;
        }

        // maintain altitude if not pressing anything
        if (forward == 0 && strafe == 0) {
            motionY = -0.01;
        }

        if (mc.options.jumpKey.isPressed()) {
            motionY = 0.03;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -0.05;
        }

        // smooth velocity blending instead of hard set (less detectable)
        Vec3d currentVel = mc.player.getVelocity();
        mc.player.setVelocity(
                currentVel.x * 0.85 + motionX,
                motionY,
                currentVel.z * 0.85 + motionZ
        );

        // cap total horizontal speed to avoid exceeding server limit
        Vec3d finalVel = mc.player.getVelocity();
        double horizSpeed = Math.sqrt(finalVel.x * finalVel.x + finalVel.z * finalVel.z);
        double maxHoriz = maxSpeed * 0.05;
        if (horizSpeed > maxHoriz) {
            double scale = maxHoriz / horizSpeed;
            mc.player.setVelocity(finalVel.x * scale, finalVel.y, finalVel.z * scale);
        }
    }

    @Override
    protected void onDisable() {
        jumpTicks = 0;
        currentSpeed = 0;
    }
}
