package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InventoryMove extends Module {

    public InventoryMove() {
        super("InventoryMove", "Allows movement while inventory screens are open", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) return;

        long window = mc.getWindow().getHandle();

        // manually check WASD and space keys
        setKeyState(mc.options.forwardKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS);
        setKeyState(mc.options.backKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS);
        setKeyState(mc.options.leftKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS);
        setKeyState(mc.options.rightKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS);
        setKeyState(mc.options.jumpKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS);
        setKeyState(mc.options.sneakKey, GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS);
    }

    private void setKeyState(KeyBinding key, boolean pressed) {
        KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()), pressed);
    }
}
