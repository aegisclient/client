package dev.aegis.client.module.movement;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class Step extends Module {

    private float stepHeight = 1.5f;

    public Step() {
        super("Step", "Step up blocks instantly via packet manipulation", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // detect when player is colliding with a block horizontally
        if (mc.player.horizontalCollision && mc.player.isOnGround()) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            // send packet sequence to step up (NCP-compatible)
            // simulate a jump and land on top
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, false));
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.753, z, false));
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.0, z, true));

            // teleport player up on client side
            mc.player.setPosition(x, y + 1.0, z);
        }
    }

    public float getStepHeight() {
        return stepHeight;
    }

    public void setStepHeight(float height) {
        this.stepHeight = height;
    }

    public static float getActiveStepHeight() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Step");
            if (mod != null && mod.isEnabled() && mod instanceof Step step) {
                return step.stepHeight;
            }
        } catch (Exception ignored) {}
        return 0.6f;
    }
}
