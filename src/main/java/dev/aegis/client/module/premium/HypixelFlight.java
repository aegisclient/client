package dev.aegis.client.module.premium;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class HypixelFlight extends Module implements EventListener {

    // clip-based flight parameters
    private static final double CLIP_DISTANCE = 4.0;   // vertical clip per phase
    private static final double GLIDE_SPEED = 0.25;    // horizontal glide speed
    private static final double FALL_SPEED = -0.0784;  // vanilla gravity value

    // state
    private int phase = 0;
    private int tickCounter = 0;
    private boolean clipping = false;
    private int clipCooldown = 0;
    private double startY = 0;

    // ground spoof
    private int groundSpoofInterval = 0;
    private boolean flagDetected = false;
    private int flagCooldown = 0;

    private final Random random = new Random();

    public HypixelFlight() {
        super("HypixelFlight", "Hypixel flight bypass using clip-based movement with ground spoof", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;
        phase = 0;
        tickCounter = 0;
        clipping = false;
        clipCooldown = 0;
        startY = mc.player.getY();
        groundSpoofInterval = 8 + random.nextInt(4);
        flagDetected = false;
        flagCooldown = 0;
    }

    @Override
    protected void onDisable() {
        phase = 0;
        clipping = false;
        flagDetected = false;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;

        // detect server position corrections (Watchdog flagging us)
        if (packetEvent.getDirection() == PacketEvent.Direction.RECEIVE) {
            if (packetEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
                flagDetected = true;
                flagCooldown = 20; // pause flight for 1 second after flag
                clipping = false;
            }
        }

        // modify outbound position packets during flight
        if (packetEvent.getDirection() == PacketEvent.Direction.SEND) {
            if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket && clipping) {
                // during clip phase, let our modified packets through
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;

        // if flagged, wait out the cooldown
        if (flagDetected) {
            flagCooldown--;
            if (flagCooldown <= 0) {
                flagDetected = false;
            }
            // during flag cooldown, apply vanilla gravity
            return;
        }

        // clip cooldown between phases
        if (clipCooldown > 0) {
            clipCooldown--;
            // during cooldown, slow fall to look more natural
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * 0.5, FALL_SPEED * 0.5, vel.z * 0.5);
            return;
        }

        // ground spoof packets - send ground=true at regular intervals
        // this prevents Watchdog's fall distance check from flagging
        if (tickCounter % groundSpoofInterval == 0) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch(), true
            ));
            // randomize next interval
            groundSpoofInterval = 7 + random.nextInt(5);
        }

        // main flight logic: clip up, then glide horizontally
        switch (phase) {
            case 0 -> doClipPhase();
            case 1 -> doGlidePhase();
            case 2 -> doFallPhase();
        }
    }

    private void doClipPhase() {
        // clip upward by sending position packets that "teleport" us up
        clipping = true;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // send a series of small position increments (Watchdog checks per-packet delta)
        double clipStep = CLIP_DISTANCE / 4.0;
        for (int i = 1; i <= 4; i++) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    x, y + clipStep * i, z, true
            ));
        }

        // set client position to match
        mc.player.setPosition(x, y + CLIP_DISTANCE, z);
        mc.player.setVelocity(0, 0, 0);

        clipping = false;
        phase = 1; // switch to glide
        tickCounter = 0;
    }

    private void doGlidePhase() {
        // horizontal glide with very slow descent
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        double motionX = 0;
        double motionZ = 0;

        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);
            motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
            motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;

            double mag = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (mag > 0) {
                motionX = (motionX / mag) * GLIDE_SPEED;
                motionZ = (motionZ / mag) * GLIDE_SPEED;
            }
        }

        // very slow descent to appear as if gliding
        double yMotion = -0.01 - random.nextDouble() * 0.005;

        mc.player.setVelocity(motionX, yMotion, motionZ);

        // glide for a limited duration then fall
        if (tickCounter > 15 + random.nextInt(5)) {
            phase = 2;
            tickCounter = 0;
        }
    }

    private void doFallPhase() {
        // controlled fall before next clip
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x * 0.8, FALL_SPEED, vel.z * 0.8);

        // after falling briefly, clip again
        if (tickCounter > 5 + random.nextInt(3)) {
            phase = 0;
            tickCounter = 0;
            clipCooldown = 2 + random.nextInt(2);
        }
    }
}
