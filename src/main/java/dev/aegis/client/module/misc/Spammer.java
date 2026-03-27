package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Spammer extends Module {

    private String message = "Aegis client on top";
    private int delay = 100; // ticks between messages
    private int ticks = 0;
    private boolean randomize = true;
    private final Random random = new Random();

    public Spammer() {
        super("Spammer", "Automatically sends chat messages at configurable intervals", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        ticks++;
        if (ticks < delay) return;
        ticks = 0;

        String msg = message;
        if (randomize) {
            // append random chars to avoid duplicate message filter
            msg += " [" + random.nextInt(9999) + "]";
        }

        mc.player.networkHandler.sendChatMessage(msg);
    }

    public void setMessage(String msg) { this.message = msg; }
    public void setDelay(int delay) { this.delay = delay; }
}
