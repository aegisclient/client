package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoDisconnect extends Module {

    private double healthThreshold = 4.0;

    public AutoDisconnect() {
        super("AutoDisconnect", "Disconnects from server when health drops critically low", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        float health = mc.player.getHealth();

        if (health > 0 && health <= healthThreshold) {
            ChatHelper.warn("Health critical (" + String.format("%.1f", health) + ") - disconnecting!");

            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().disconnect(
                        Text.literal("[Aegis] AutoDisconnect: Health dropped to " + String.format("%.1f", health))
                );
            }

            // disable after triggering so it doesn't loop
            this.disable();
        }
    }
}
