package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Macros extends Module {

    private final Map<Integer, String> macros = new HashMap<>();

    public Macros() {
        super("Macros", "Bind chat commands or messages to keys", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        long window = mc.getWindow().getHandle();
        for (Map.Entry<Integer, String> entry : macros.entrySet()) {
            if (GLFW.glfwGetKey(window, entry.getKey()) == GLFW.GLFW_PRESS) {
                String msg = entry.getValue();
                if (msg.startsWith("/")) {
                    mc.player.networkHandler.sendChatCommand(msg.substring(1));
                } else {
                    mc.player.networkHandler.sendChatMessage(msg);
                }
            }
        }
    }

    public void addMacro(int key, String command) {
        macros.put(key, command);
    }

    public void removeMacro(int key) {
        macros.remove(key);
    }
}
