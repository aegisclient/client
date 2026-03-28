package dev.aegis.client.module.premium;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ServerBypasses extends Module implements EventListener {

    public enum Mode { VULCAN, MATRIX, GRIM, UNIVERSAL }
    private Mode mode = Mode.UNIVERSAL;

    // universal bypass state
    private int tickCounter = 0;
    private final Queue<TeleportConfirmC2SPacket> heldTeleports = new LinkedList<>();
    private int teleportReleaseDelay = 0;

    // Vulcan-specific: ground spoof timing
    private int vulcanGroundTimer = 0;

    // Matrix-specific: movement packet throttle
    private int matrixPacketCount = 0;
    private int matrixWindow = 0;

    // Grim-specific: prediction desync
    private boolean grimDesync = false;
    private int grimDesyncTicks = 0;

    private final Random random = new Random();

    public ServerBypasses() {
        super("ServerBypasses", "Generic anticheat bypasses for Vulcan, Matrix, Grim, and more", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
        heldTeleports.clear();
        teleportReleaseDelay = 0;
        vulcanGroundTimer = 0;
        matrixPacketCount = 0;
        matrixWindow = 0;
        grimDesync = false;
        grimDesyncTicks = 0;

        ChatHelper.info("ServerBypasses mode: \u00a7e" + mode.name());
    }

    @Override
    protected void onDisable() {
        // release held teleport confirms
        if (mc.getNetworkHandler() != null) {
            while (!heldTeleports.isEmpty()) {
                mc.getNetworkHandler().sendPacket(heldTeleports.poll());
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;

        switch (mode) {
            case VULCAN -> handleVulcanPackets(packetEvent);
            case MATRIX -> handleMatrixPackets(packetEvent);
            case GRIM -> handleGrimPackets(packetEvent);
            case UNIVERSAL -> handleUniversalPackets(packetEvent);
        }
    }

    private void handleVulcanPackets(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.SEND) return;

        // Vulcan bypass: manipulate teleport confirms and ground state
        if (event.getPacket() instanceof TeleportConfirmC2SPacket teleport) {
            // delay every other teleport confirm
            if (tickCounter % 2 == 0) {
                heldTeleports.add(teleport);
                teleportReleaseDelay = 3;
                event.cancel();
            }
        }
    }

    private void handleMatrixPackets(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.SEND) return;

        // Matrix bypass: throttle movement packets to avoid triggering packet-rate checks
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            matrixPacketCount++;
            // Matrix flags >22 movement packets per second
            if (matrixPacketCount > 20 && matrixWindow < 20) {
                event.cancel();
            }
        }
    }

    private void handleGrimPackets(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.SEND) return;

        // Grim bypass: prediction desync via position manipulation
        if (event.getPacket() instanceof PlayerMoveC2SPacket && grimDesync) {
            // during desync phase, let packets through but with modified ground state
        }

        if (event.getPacket() instanceof TeleportConfirmC2SPacket teleport) {
            // Grim uses teleport transaction matching - delay to desync
            heldTeleports.add(teleport);
            teleportReleaseDelay = 2 + random.nextInt(2);
            event.cancel();
        }
    }

    private void handleUniversalPackets(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.SEND) return;

        // Universal: conservative approach that works on most anticheats
        if (event.getPacket() instanceof TeleportConfirmC2SPacket teleport) {
            if (tickCounter % 3 == 0) {
                heldTeleports.add(teleport);
                teleportReleaseDelay = 4 + random.nextInt(3);
                event.cancel();
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;

        // release held teleport confirms
        if (teleportReleaseDelay > 0) {
            teleportReleaseDelay--;
            if (teleportReleaseDelay == 0 && !heldTeleports.isEmpty()) {
                while (!heldTeleports.isEmpty()) {
                    mc.getNetworkHandler().sendPacket(heldTeleports.poll());
                }
            }
        }

        switch (mode) {
            case VULCAN -> tickVulcan();
            case MATRIX -> tickMatrix();
            case GRIM -> tickGrim();
            case UNIVERSAL -> tickUniversal();
        }
    }

    private void tickVulcan() {
        vulcanGroundTimer++;

        // Vulcan ground spoof: send ground=true position every 30 ticks
        // overflows Vulcan's VL accumulator for fly/speed checks
        if (vulcanGroundTimer >= 28 + random.nextInt(5)) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch(), true
            ));
            vulcanGroundTimer = 0;
        }
    }

    private void tickMatrix() {
        matrixWindow++;
        // reset packet count every second (20 ticks)
        if (matrixWindow >= 20) {
            matrixWindow = 0;
            matrixPacketCount = 0;
        }

        // send micro-position updates to keep Matrix's movement tracker confused
        if (tickCounter % (15 + random.nextInt(5)) == 0) {
            double offsetX = (random.nextDouble() - 0.5) * 0.00001;
            double offsetZ = (random.nextDouble() - 0.5) * 0.00001;

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX() + offsetX, mc.player.getY(), mc.player.getZ() + offsetZ,
                    mc.player.isOnGround()
            ));
        }
    }

    private void tickGrim() {
        // Grim prediction desync: toggle desync every ~50 ticks
        if (tickCounter % (45 + random.nextInt(10)) == 0) {
            grimDesync = !grimDesync;
            if (grimDesync) {
                grimDesyncTicks = 10 + random.nextInt(5);
            }
        }

        if (grimDesync) {
            grimDesyncTicks--;
            if (grimDesyncTicks <= 0) {
                grimDesync = false;
            }

            // during desync: send duplicate position packet with slightly different Y
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY() + 0.000001, mc.player.getZ(),
                    mc.player.isOnGround()
            ));
        }
    }

    private void tickUniversal() {
        // periodic ground spoof
        if (tickCounter % (35 + random.nextInt(10)) == 0) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
            ));
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (isEnabled()) {
            ChatHelper.info("ServerBypasses mode: \u00a7e" + mode.name());
        }
    }

    public Mode getMode() { return mode; }
}
