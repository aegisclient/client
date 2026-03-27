package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class CameraClip extends Module {

    public CameraClip() {
        super("CameraClip", "Allows camera to clip through blocks in third person", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled via mixin on Camera.clipToSpace
    }
}
