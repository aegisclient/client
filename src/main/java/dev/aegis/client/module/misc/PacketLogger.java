package dev.aegis.client.module.misc;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import org.lwjgl.glfw.GLFW;

public class PacketLogger extends Module implements EventListener {

    private boolean logSend = true;
    private boolean logReceive = false;

    public PacketLogger() {
        super("PacketLogger", "Logs all sent and received packets to chat", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;

        String packetName = packetEvent.getPacket().getClass().getSimpleName();

        if (packetEvent.getDirection() == PacketEvent.Direction.SEND && logSend) {
            ChatHelper.info("\u00a7b[OUT] \u00a7f" + packetName);
        }
        if (packetEvent.getDirection() == PacketEvent.Direction.RECEIVE && logReceive) {
            ChatHelper.info("\u00a7a[IN] \u00a7f" + packetName);
        }
    }

    @Override
    public void onTick() {
        // logging handled in onEvent
    }
}
