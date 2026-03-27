package dev.aegis.client.module.fun;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.render.entity.PlayerModelPart;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class SkinDerp extends Module {

    private final Random random = new Random();
    private int tickCounter = 0;

    public SkinDerp() {
        super("SkinDerp", "Rapidly toggles skin layer parts for a glitchy effect", Category.FUN, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        tickCounter++;
        if (tickCounter % 2 != 0) return;

        for (PlayerModelPart part : PlayerModelPart.values()) {
            mc.options.togglePlayerModelPart(part, random.nextBoolean());
        }
    }

    @Override
    protected void onDisable() {
        // re-enable all parts
        for (PlayerModelPart part : PlayerModelPart.values()) {
            mc.options.togglePlayerModelPart(part, true);
        }
    }
}
