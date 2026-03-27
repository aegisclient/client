package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Spider extends Module {

    private double climbSpeed = 0.2;

    public Spider() {
        super("Spider", "Allows climbing any wall like a spider", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.horizontalCollision) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, climbSpeed, vel.z);
            mc.player.fallDistance = 0;
        }
    }
}
