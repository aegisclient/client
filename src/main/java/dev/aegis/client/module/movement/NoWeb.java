package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class NoWeb extends Module {

    public NoWeb() {
        super("NoWeb", "Prevents cobwebs from slowing you down", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // if player is very slow and on ground in a web, boost them out
        // actual web slowdown prevention handled via mixin
    }
}
