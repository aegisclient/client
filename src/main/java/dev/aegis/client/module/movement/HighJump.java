package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class HighJump extends Module {

    private double jumpBoost = 0.5;
    private boolean boosted = false;

    public HighJump() {
        super("HighJump", "Increases jump height beyond normal limits", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.isOnGround()) {
            boosted = false;
        }

        if (!mc.player.isOnGround() && !boosted && mc.player.getVelocity().y > 0.1) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, vel.y + jumpBoost, vel.z);
            boosted = true;
        }
    }
}
