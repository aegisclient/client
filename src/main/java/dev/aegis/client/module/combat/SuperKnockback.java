package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

public class SuperKnockback extends Module {

    public SuperKnockback() {
        super("SuperKnockback", "Applies extra knockback to attacked entities via sprint reset", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // handled by doKnockback() called from KillAura/TriggerBot
    }

    public void doKnockback() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // sprint reset trick: stop sprinting then start again before the hit lands
        mc.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING)
        );
        mc.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING)
        );
    }
}
