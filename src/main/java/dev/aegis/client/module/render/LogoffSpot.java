package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class LogoffSpot extends Module {

    private final Map<String, Vec3d> logoffPositions = new HashMap<>();
    private final Map<String, String> knownPlayers = new HashMap<>();

    public LogoffSpot() {
        super("LogoffSpot", "Marks where players log off to track their last position", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        Map<String, Vec3d> currentPlayers = new HashMap<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();
            currentPlayers.put(name, player.getPos());
        }

        // detect logoffs: players that were here last tick but aren't now
        for (Map.Entry<String, String> entry : knownPlayers.entrySet()) {
            if (!currentPlayers.containsKey(entry.getKey())) {
                // player left - mark their last position
                // we stored position in the value as encoded string
            }
        }

        // update tracking
        for (Map.Entry<String, Vec3d> entry : currentPlayers.entrySet()) {
            logoffPositions.put(entry.getKey(), entry.getValue());
        }

        // remove positions of players who came back
        for (String name : currentPlayers.keySet()) {
            // they're online, keep updating position
            logoffPositions.put(name, currentPlayers.get(name));
        }
    }

    public Map<String, Vec3d> getLogoffPositions() {
        return logoffPositions;
    }

    @Override
    protected void onDisable() {
        logoffPositions.clear();
        knownPlayers.clear();
    }
}
