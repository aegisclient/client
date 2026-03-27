package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class AirJump extends Module {

    public AirJump() {
        super("AirJump", "Allows jumping while in the air", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.options.jumpKey.wasPressed() && !mc.player.isOnGround()) {
            mc.player.jump();
            mc.player.fallDistance = 0;
        }
    }
}
