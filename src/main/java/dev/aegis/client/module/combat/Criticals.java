package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Criticals extends Module {

    public enum Mode { PACKET, MINI_JUMP, NO_GROUND, EDIT }

    private Mode mode = Mode.PACKET;
    private final Random random = new Random();
    private int cooldown = 0;
    private int editTicks = 0;

    public Criticals() {
        super("Criticals", "Packet-based critical hits with NCP-bypass offsets", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) cooldown--;

        // edit mode: periodically jump micro amounts to stay "airborne"
        if (mode == Mode.EDIT) {
            editTicks++;
        }
    }

    /**
     * Call this before attacking to trigger a critical hit.
     * Called by KillAura and other combat modules.
     */
    public void doCrit() {
        if (mc.player == null) return;
        if (cooldown > 0) return;

        switch (mode) {
            case PACKET -> doPacketCrit();
            case MINI_JUMP -> doMiniJumpCrit();
            case NO_GROUND -> doNoGroundCrit();
            case EDIT -> doEditCrit();
        }
    }

    private void doPacketCrit() {
        // requires on ground for the jump sequence
        if (!mc.player.isOnGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // NCP-bypass packet sequence with randomized micro-offsets
        // the offsets need to be small enough to not trigger "moving too quickly"
        // but large enough to register as airborne for the critical check
        double offset1 = 0.0625 + random.nextDouble() * 0.003;
        double offset2 = 1.0E-5 + random.nextDouble() * 2.0E-6;
        double offset3 = 0.015 + random.nextDouble() * 0.002;

        // 4-packet sequence: up, down, tiny up, down
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + offset1, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + offset2, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));

        cooldown = 2 + random.nextInt(2);
    }

    private void doMiniJumpCrit() {
        // actual mini-jump: gives a tiny real upward velocity
        // looks more natural than packet-only
        if (!mc.player.isOnGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // simulate a very small jump
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.1, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.04, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0025, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));

        cooldown = 3;
    }

    private void doNoGroundCrit() {
        // no-ground mode: never sends onGround=true
        // simpler bypass, works on some anti-cheats
        if (!mc.player.isOnGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.049, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));

        cooldown = 1;
    }

    private void doEditCrit() {
        // edit mode: designed for strict anti-cheats
        // uses very small offsets that look like normal movement jitter
        if (!mc.player.isOnGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // movement jitter pattern that triggers crit
        double microOffset = 0.01 + random.nextDouble() * 0.005;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + microOffset, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + microOffset * 0.1, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));

        cooldown = 2 + random.nextInt(3);
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }
}
