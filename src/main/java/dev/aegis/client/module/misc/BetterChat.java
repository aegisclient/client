package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class BetterChat extends Module {

    private boolean antiSpam = true;
    private boolean timestamps = true;
    private boolean infiniteHistory = true;

    public BetterChat() {
        super("BetterChat", "Chat improvements: anti-spam, timestamps, infinite history", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // chat modifications handled via mixin on ChatHud
    }

    public boolean isAntiSpam() { return antiSpam; }
    public boolean hasTimestamps() { return timestamps; }
    public boolean isInfiniteHistory() { return infiniteHistory; }
}
