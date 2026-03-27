package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class Notifier extends Module {

    private final Set<String> trackedPlayers = new HashSet<>();
    private boolean visualRange = true;
    private boolean totemPops = true;

    public Notifier() {
        super("Notifier", "Alerts you when players enter visual range or pop totems", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        if (visualRange) {
            Set<String> current = new HashSet<>();
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                String name = player.getName().getString();
                current.add(name);

                if (!trackedPlayers.contains(name)) {
                    ChatHelper.info("\u00a7e" + name + "\u00a7f entered visual range (" +
                            String.format("%.0f", mc.player.distanceTo(player)) + " blocks)");
                }
            }

            // check who left
            for (String name : trackedPlayers) {
                if (!current.contains(name)) {
                    ChatHelper.info("\u00a7e" + name + "\u00a7f left visual range");
                }
            }

            trackedPlayers.clear();
            trackedPlayers.addAll(current);
        }
    }

    @Override
    protected void onDisable() {
        trackedPlayers.clear();
    }
}
