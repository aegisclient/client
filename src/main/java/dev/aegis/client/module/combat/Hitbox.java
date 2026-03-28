package dev.aegis.client.module.combat;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Hitbox extends Module {

    private double expand = 0.4;

    public Hitbox() {
        super("Hitbox", "Expands entity hitboxes to make them easier to hit", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // hitbox expansion applied via mixin on entity targeting
    }

    public double getExpand() {
        return expand;
    }

    public void setExpand(double expand) {
        this.expand = expand;
    }

    /**
     * Returns whether hitbox expansion should be active.
     * Called from mixins.
     */
    public static boolean shouldExpand() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Hitbox");
            return mod != null && mod.isEnabled();
        } catch (Exception ignored) {}
        return false;
    }

    public static double getExpandAmount() {
        try {
            Module mod = Aegis.getInstance().getModuleManager().getModule("Hitbox");
            if (mod != null && mod.isEnabled() && mod instanceof Hitbox hitbox) {
                return hitbox.expand;
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}
