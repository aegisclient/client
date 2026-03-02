package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Step extends Module {

    private float stepHeight = 2.0f;

    public Step() {
        super("Step", "Step up blocks instantly", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // step height is handled in MixinLivingEntity.getStepHeight
    }

    @Override
    protected void onDisable() {
        // reset happens automatically since mixin checks if module is enabled
    }

    public float getStepHeight() {
        return stepHeight;
    }

    public void setStepHeight(float height) {
        this.stepHeight = height;
    }
}
