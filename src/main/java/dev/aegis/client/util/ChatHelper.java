package dev.aegis.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatHelper {

    private static final String PREFIX = "\u00a76[\u00a7eAegis\u00a76] \u00a7r";

    public static void info(String message) {
        send(PREFIX + message);
    }

    public static void warn(String message) {
        send(PREFIX + "\u00a7e" + message);
    }

    public static void error(String message) {
        send(PREFIX + "\u00a7c" + message);
    }

    public static void success(String message) {
        send(PREFIX + "\u00a7a" + message);
    }

    private static void send(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), false);
        }
    }
}
