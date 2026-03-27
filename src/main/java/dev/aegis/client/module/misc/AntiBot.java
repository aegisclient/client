package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class AntiBot extends Module {

    private final Set<Integer> botIds = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "Detects and filters fake players spawned by anti-cheats", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        botIds.clear();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            if (isBot(player)) {
                botIds.add(player.getId());
            }
        }
    }

    private boolean isBot(PlayerEntity player) {
        // common bot detection heuristics
        if (player.getGameProfile() == null) return true;

        // bots often have no listed ping in tab
        if (mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry == null) return true;
            if (entry.getLatency() == 0) return true;
        }

        // bots often have weird names
        String name = player.getName().getString();
        if (name.length() > 16 || name.isEmpty()) return true;

        return false;
    }

    public boolean isBot(int entityId) {
        return botIds.contains(entityId);
    }

    @Override
    protected void onDisable() {
        botIds.clear();
    }
}
