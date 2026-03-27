package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {

    private int packetCount = 0;

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // only send packets when actually falling significant distance
        // use alternating pattern to avoid NCP packet analysis
        if (mc.player.fallDistance > 2.0f) {
            packetCount++;

            // don't spam every tick - send every other tick to look natural
            if (packetCount % 2 == 0) {
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.OnGroundOnly(true)
                );
            }

            // also send a position packet occasionally to reinforce ground state
            if (packetCount % 6 == 0) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                );
            }
        } else {
            packetCount = 0;
        }
    }

    @Override
    protected void onDisable() {
        packetCount = 0;
    }
}
