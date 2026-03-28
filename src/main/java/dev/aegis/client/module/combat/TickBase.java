package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class TickBase extends Module {

    private int ticksToShift = 5;
    private double activateRange = 4.0;
    private boolean charging = false;
    private int chargedTicks = 0;
    private Vec3d chargeStartPos = null;

    public TickBase() {
        super("TickBase", "Manipulates client tick timing to gain advantage in combat", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        charging = false;
        chargedTicks = 0;
        chargeStartPos = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(LivingEntity::isAlive)
                .filter(p -> mc.player.distanceTo(p) <= activateRange + 3)
                .min((a, b) -> Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b)))
                .orElse(null);

        if (target == null) {
            if (charging) releaseCharge();
            return;
        }

        double dist = mc.player.distanceTo(target);

        if (!charging && dist > activateRange) {
            // start charging - save position
            charging = true;
            chargedTicks = 0;
            chargeStartPos = mc.player.getPos();
        }

        if (charging) {
            chargedTicks++;

            // freeze movement by zeroing velocity during charge
            if (chargeStartPos != null) {
                mc.player.setVelocity(Vec3d.ZERO);
            }

            // release when enough ticks charged or target in range
            if (chargedTicks >= ticksToShift || dist <= activateRange) {
                releaseCharge();
            }
        }
    }

    private void releaseCharge() {
        if (!charging || mc.player == null) return;

        // send rapid position updates to simulate multiple ticks of movement
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        for (int i = 0; i < chargedTicks; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.Full(x, y, z,
                            mc.player.getYaw(), mc.player.getPitch(),
                            mc.player.isOnGround())
            );
        }

        charging = false;
        chargedTicks = 0;
        chargeStartPos = null;
    }

    @Override
    protected void onDisable() {
        if (charging) releaseCharge();
        charging = false;
        chargedTicks = 0;
        chargeStartPos = null;
    }

    public void setTicksToShift(int ticks) { this.ticksToShift = ticks; }
    public void setActivateRange(double range) { this.activateRange = range; }
}
