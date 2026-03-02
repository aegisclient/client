package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Always sprint when moving forward", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // actual sprint logic is in MixinClientPlayerEntity for better timing
        // this module just needs to exist so the mixin can check if it's enabled
    }
}
