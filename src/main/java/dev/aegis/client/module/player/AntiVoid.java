package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AntiVoid extends Module {

    private Vec3d safePosition;
    private double voidLevel;

    public AntiVoid() {
        super("AntiVoid", "Teleports you back to safety when falling into the void", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        voidLevel = mc.world.getBottomY();

        // save safe position when on ground
        if (mc.player.isOnGround() && mc.player.getY() > voidLevel + 5) {
            safePosition = mc.player.getPos();
        }

        // detect void fall
        if (mc.player.getY() < voidLevel + 1 && safePosition != null) {
            mc.player.setPosition(safePosition.x, safePosition.y, safePosition.z);
            mc.player.setVelocity(0, 0, 0);
            mc.player.fallDistance = 0;
        }
    }
}
