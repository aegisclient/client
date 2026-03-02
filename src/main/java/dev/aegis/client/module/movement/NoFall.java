package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // if we're falling more than 3 blocks, send a fake onGround packet
        if (mc.player.fallDistance > 2.5f) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.OnGroundOnly(true)
            );
        }
    }
}
