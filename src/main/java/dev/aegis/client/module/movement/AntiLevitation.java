package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class AntiLevitation extends Module {

    public AntiLevitation() {
        super("AntiLevitation", "Removes levitation and slow falling effects", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        }
    }
}
