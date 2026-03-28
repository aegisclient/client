package dev.aegis.client.module.premium;

import dev.aegis.client.Aegis;
import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class HypixelDisabler extends Module implements EventListener {

    // teleport confirm manipulation
    private final Queue<TeleportConfirmC2SPacket> delayedTeleports = new LinkedList<>();
    private int teleportReleaseTimer = 0;

    // keep-alive timing manipulation
    private long lastKeepAlive = 0;
    private boolean delayNextKeepAlive = false;
    private int keepAliveDelayTicks = 0;

    // movement packet manipulation
    private int tickCounter = 0;
    private boolean groundSpoofActive = false;
    private int groundSpoofTicks = 0;

    // watchdog desync state
    private double serverX, serverY, serverZ;
    private boolean positionDesynced = false;
    private int desyncTicks = 0;

    private final Random random = new Random();

    public HypixelDisabler() {
        super("HypixelDisabler", "Watchdog anticheat bypass via packet manipulation", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
        teleportReleaseTimer = 0;
        delayedTeleports.clear();
        lastKeepAlive = System.currentTimeMillis();
        delayNextKeepAlive = false;
        keepAliveDelayTicks = 0;
        groundSpoofActive = false;
        groundSpoofTicks = 0;
        positionDesynced = false;
        desyncTicks = 0;
    }

    @Override
    protected void onDisable() {
        // release all held teleport confirms
        if (mc.getNetworkHandler() != null) {
            while (!delayedTeleports.isEmpty()) {
                mc.getNetworkHandler().sendPacket(delayedTeleports.poll());
            }
        }
        tickCounter = 0;
        positionDesynced = false;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;

        if (packetEvent.getDirection() == PacketEvent.Direction.SEND) {
            handleOutboundPacket(packetEvent);
        } else {
            handleInboundPacket(packetEvent);
        }
    }

    private void handleOutboundPacket(PacketEvent event) {
        // teleport confirm manipulation - delay confirms to desync Watchdog's position tracker
        if (event.getPacket() instanceof TeleportConfirmC2SPacket teleportConfirm) {
            // hold teleport confirms and release them in bursts
            delayedTeleports.add(teleportConfirm);
            teleportReleaseTimer = 4 + random.nextInt(3); // release after 4-6 ticks
            event.cancel();
            return;
        }

        // keep-alive timing jitter - makes Watchdog's latency calculations unreliable
        if (event.getPacket() instanceof KeepAliveC2SPacket) {
            if (delayNextKeepAlive) {
                event.cancel();
                keepAliveDelayTicks = 2 + random.nextInt(3); // delay 2-4 ticks
                delayNextKeepAlive = false;
                return;
            }
            lastKeepAlive = System.currentTimeMillis();
            // randomly delay every ~4th keep-alive
            if (random.nextInt(4) == 0) {
                delayNextKeepAlive = true;
            }
        }

        // movement packet manipulation - inject ground spoof on specific intervals
        if (event.getPacket() instanceof PlayerMoveC2SPacket movePacket) {
            if (groundSpoofActive && groundSpoofTicks > 0) {
                // modify the ground state to confuse Watchdog's movement checks
                groundSpoofTicks--;
                if (groundSpoofTicks == 0) {
                    groundSpoofActive = false;
                }
            }
        }
    }

    private void handleInboundPacket(PacketEvent event) {
        // intercept server position corrections to maintain desync
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket posLook) {
            serverX = posLook.getX();
            serverY = posLook.getY();
            serverZ = posLook.getZ();

            // track that we got a position correction - Watchdog flagging movement
            if (positionDesynced) {
                desyncTicks = 0;
                positionDesynced = false;
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;

        // release delayed teleport confirms in bursts
        if (teleportReleaseTimer > 0) {
            teleportReleaseTimer--;
            if (teleportReleaseTimer == 0 && !delayedTeleports.isEmpty()) {
                // release all at once to create a burst that confuses the position tracker
                while (!delayedTeleports.isEmpty()) {
                    mc.getNetworkHandler().sendPacket(delayedTeleports.poll());
                }
            }
        }

        // release delayed keep-alive
        if (keepAliveDelayTicks > 0) {
            keepAliveDelayTicks--;
            if (keepAliveDelayTicks == 0) {
                mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(lastKeepAlive));
            }
        }

        // periodic ground spoof - sends a ground=true position every ~40 ticks
        // this overflows Watchdog's VL buffer for movement checks
        if (tickCounter % (38 + random.nextInt(5)) == 0) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), true)
            );
            groundSpoofActive = true;
            groundSpoofTicks = 2;
        }

        // micro position offset - tiny C03 packets to desync the server position tracker
        if (tickCounter % (20 + random.nextInt(10)) == 0) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            // offset so small it won't trigger invalid-move but large enough to desync tracking
            double offsetX = (random.nextDouble() - 0.5) * 0.00002;
            double offsetZ = (random.nextDouble() - 0.5) * 0.00002;

            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x + offsetX, y, z + offsetZ, mc.player.isOnGround())
            );
            positionDesynced = true;
            desyncTicks = 0;
        }

        // track desync duration
        if (positionDesynced) {
            desyncTicks++;
            // if desynced too long, reset to avoid flagging
            if (desyncTicks > 60) {
                positionDesynced = false;
                desyncTicks = 0;
            }
        }
    }
}
