package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoLeave extends Module {

    private double healthThreshold = 8.0;
    private double enemyRange = 32.0;

    public AutoLeave() {
        super("AutoLeave", "Disconnects when a player enters range while health is low", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.getHealth() > healthThreshold) return;

        boolean enemyNearby = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .anyMatch(p -> mc.player.distanceTo(p) <= enemyRange);

        if (enemyNearby) {
            mc.player.networkHandler.getConnection().disconnect(
                    Text.of("[Aegis] AutoLeave - enemy detected while low HP")
            );
            disable();
        }
    }
}
