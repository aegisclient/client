package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {

    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "See perfectly in the dark", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // apply night vision effect for maximum brightness
        mc.player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false)
        );
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}
