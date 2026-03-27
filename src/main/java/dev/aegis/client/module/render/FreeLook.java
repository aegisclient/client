package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends Module {

    private float savedYaw;
    private float savedPitch;
    private boolean locked = false;

    public FreeLook() {
        super("FreeLook", "Look around freely without changing movement direction", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            savedYaw = mc.player.getYaw();
            savedPitch = mc.player.getPitch();
            locked = true;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // perspective is set to third person in mixin when this is active
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && locked) {
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
            locked = false;
        }
    }

    public float getSavedYaw() { return savedYaw; }
    public float getSavedPitch() { return savedPitch; }
    public boolean isLocked() { return locked; }
}
