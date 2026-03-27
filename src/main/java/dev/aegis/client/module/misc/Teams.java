package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class Teams extends Module {

    public Teams() {
        super("Teams", "Detects team members by armor color and prevents targeting them", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // team detection is queried by combat modules
    }

    public boolean isOnSameTeam(PlayerEntity player) {
        if (mc.player == null) return false;

        // check scoreboard team
        if (mc.player.getScoreboardTeam() != null && player.getScoreboardTeam() != null) {
            return mc.player.getScoreboardTeam().isEqual(player.getScoreboardTeam());
        }

        return false;
    }
}
