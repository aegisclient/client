package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoMissCooldown extends Module {

    public NoMissCooldown() {
        super("NoMissCooldown", "Resets attack cooldown even when you miss", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // constantly reset the attack cooldown timer
        // this makes it so missing a hit doesn't slow you down
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) {
            mc.player.resetLastAttackedTicks();
        }
    }
}
