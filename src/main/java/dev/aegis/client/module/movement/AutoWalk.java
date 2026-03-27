package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", "Automatically holds the forward key", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        KeyBinding.setKeyPressed(
                InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()),
                true
        );
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            KeyBinding.setKeyPressed(
                    InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()),
                    false
            );
        }
    }
}
