package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class BlockOutline extends Module {

    private float red = 1.0f;
    private float green = 0.84f;
    private float blue = 0.0f;
    private float alpha = 0.8f;
    private float lineWidth = 2.0f;

    public BlockOutline() {
        super("BlockOutline", "Customizes the block selection outline color and width", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled via mixin on WorldRenderer.drawBlockOutline
    }

    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public float getAlpha() { return alpha; }
    public float getLineWidth() { return lineWidth; }
}
