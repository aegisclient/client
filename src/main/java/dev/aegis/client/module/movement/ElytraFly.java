package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ElytraFly extends Module {

    private double speed = 1.8;
    private boolean autoOpen = true;
    private int jumpTicks = 0;

    public ElytraFly() {
        super("ElytraFly", "Fly with elytra at controlled speed, auto-opens on jump", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
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

        if (!mc.player.isFallFlying()) return;

        // controlled flight
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        float pitch = (float) Math.toRadians(mc.player.getPitch());

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double motionX = 0, motionY = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            double cos = Math.cos(yaw);
            double sin = Math.sin(yaw);
            motionX = (-sin * forward + cos * strafe) * speed * 0.05;
            motionZ = (cos * forward + sin * strafe) * speed * 0.05;
            motionY = -Math.sin(pitch) * forward * speed * 0.05;
        }

        // maintain altitude if not pressing anything
        if (forward == 0 && strafe == 0) {
            motionY = -0.01; // very slow descent
        }

        if (mc.options.jumpKey.isPressed()) {
            motionY = speed * 0.03;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -speed * 0.05;
        }

        mc.player.setVelocity(
                mc.player.getVelocity().x * 0.9 + motionX,
                motionY,
                mc.player.getVelocity().z * 0.9 + motionZ
        );
    }

    @Override
    protected void onDisable() {
        jumpTicks = 0;
    }
}
