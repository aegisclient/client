package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Criticals extends Module {

    private final Random random = new Random();
    private int cooldown = 0;

    public Criticals() {
        super("Criticals", "Makes every hit a critical hit", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) cooldown--;
    }

    public void doCrit() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (cooldown > 0) return;

        // NCP-bypass crit packet sequence
        // uses slightly randomized offsets to avoid pattern detection
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // add micro-randomization to offsets (within valid NCP tolerance)
        double offset1 = 0.0625 + random.nextDouble() * 0.002;
        double offset2 = 1.1e-5 + random.nextDouble() * 1e-6;

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + offset1, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + offset2, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));

        // small cooldown to prevent packet spam flags
        cooldown = 2;
    }
}
