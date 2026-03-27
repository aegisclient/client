package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class TrueSight extends Module {

    public TrueSight() {
        super("TrueSight", "See invisible entities and barriers", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled via mixin to make invisible entities visible
        // and barriers renderable
    }
}
