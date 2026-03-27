package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoClicker extends Module {

    private int minCps = 8;
    private int maxCps = 12;
    private int delay = 0;
    private final Random random = new Random();

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks at a configurable CPS range", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int cps = minCps + random.nextInt(maxCps - minCps + 1);
        delay = 20 / cps + (random.nextBoolean() ? 1 : 0);

        if (mc.targetedEntity != null) {
            mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
