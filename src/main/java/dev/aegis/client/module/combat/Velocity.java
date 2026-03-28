package dev.aegis.client.module.combat;

import dev.aegis.client.event.Event;
import dev.aegis.client.event.EventListener;
import dev.aegis.client.event.events.PacketEvent;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Velocity extends Module implements EventListener {

    public enum Mode { CANCEL, REDUCE, GRIM, JUMP_RESET, REVERSE }

    private Mode mode = Mode.CANCEL;

    // percentage-based velocity modification (0.0 = full cancel, 1.0 = vanilla)
    private double horizontal = 0.0;
    private double vertical = 0.0;

    // separate horizontal and vertical control
    private boolean separateControl = true; // when true, H and V are independently adjustable

    // chance-based: occasionally let full KB through to look legit
    private boolean chanceBypass = false;
    private double letThroughChance = 0.05; // 5% chance to take full KB

    // grim bypass state
    private int grimTicks = 0;
    private boolean wasHit = false;
    private int grimSprintToggleCount = 3; // number of sprint toggles for grim bypass

    // jump reset state
    private boolean needsJump = false;
    private int jumpResetDelay = 0; // ticks to wait before jump-resetting
    private int currentJumpDelay = 0;

    // reverse velocity mode
    private double reverseHorizontal = -0.3; // negative = push back toward attacker
    private double reverseVertical = 0.0;
    private boolean reverseOnlyPlayers = true; // only reverse KB from players

    // adaptive reduction: gradually reduce KB more as fight continues
    private boolean adaptiveReduction = false;
    private int hitCount = 0;
    private double adaptiveMinHorizontal = 0.0;
    private double adaptiveStartHorizontal = 0.8;
    private int adaptiveRampHits = 5; // hits to reach minimum

    private final Random random = new Random();

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback from hits and explosions", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        grimTicks = 0;
        wasHit = false;
        needsJump = false;
        hitCount = 0;
        currentJumpDelay = 0;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof PacketEvent packetEvent)) return;
        if (packetEvent.getDirection() != PacketEvent.Direction.RECEIVE) return;
        if (mc.player == null) return;

        // chance bypass: occasionally let KB through to appear legit
        if (chanceBypass && random.nextDouble() < letThroughChance) return;

        if (packetEvent.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getId() != mc.player.getId()) return;

            hitCount++;

            switch (mode) {
                case CANCEL -> {
                    double effectiveH = getEffectiveHorizontal();
                    double effectiveV = getEffectiveVertical();
                    if (effectiveH == 0 && effectiveV == 0) {
                        packetEvent.cancel();
                    }
                    // partial reduction handled in onTick
                }
                case REDUCE -> {
                    // let packet through, modify in onTick
                    wasHit = true;
                }
                case GRIM -> {
                    // grim bypass: cancel packet, then do sprint toggle trick
                    packetEvent.cancel();
                    grimTicks = 1;
                }
                case JUMP_RESET -> {
                    wasHit = true;
                    needsJump = true;
                    currentJumpDelay = jumpResetDelay;
                }
                case REVERSE -> {
                    // cancel original and apply reversed velocity in onTick
                    packetEvent.cancel();
                    wasHit = true;
                }
            }
        }

        if (packetEvent.getPacket() instanceof ExplosionS2CPacket) {
            hitCount++;

            switch (mode) {
                case CANCEL, GRIM -> {
                    double effectiveH = getEffectiveHorizontal();
                    double effectiveV = getEffectiveVertical();
                    if (effectiveH == 0 && effectiveV == 0) {
                        packetEvent.cancel();
                    }
                }
                case REDUCE -> wasHit = true;
                case JUMP_RESET -> {
                    wasHit = true;
                    needsJump = true;
                    currentJumpDelay = jumpResetDelay;
                }
                case REVERSE -> {
                    packetEvent.cancel();
                    wasHit = true;
                }
            }
        }
    }

    private double getEffectiveHorizontal() {
        if (adaptiveReduction) {
            // ramp from start to min over adaptiveRampHits
            double progress = Math.min(1.0, (double) hitCount / adaptiveRampHits);
            return adaptiveStartHorizontal - (adaptiveStartHorizontal - adaptiveMinHorizontal) * progress;
        }
        return horizontal;
    }

    private double getEffectiveVertical() {
        if (adaptiveReduction) {
            // vertical adapts less aggressively
            double progress = Math.min(1.0, (double) hitCount / (adaptiveRampHits * 2));
            return vertical * (1.0 - progress * 0.5);
        }
        return vertical;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        switch (mode) {
            case CANCEL -> handleCancel();
            case REDUCE -> handleReduce();
            case GRIM -> handleGrim();
            case JUMP_RESET -> handleJumpReset();
            case REVERSE -> handleReverse();
        }

        // reset hit count when not being hit for a while
        if (mc.player.hurtTime == 0 && hitCount > 0) {
            // decay hit count over time
            if (random.nextInt(20) == 0) {
                hitCount = Math.max(0, hitCount - 1);
            }
        }
    }

    private void handleCancel() {
        // full cancel mode with percentage-based reduction support
        if (mc.player.hurtTime > 0) {
            double effectiveH = getEffectiveHorizontal();
            double effectiveV = getEffectiveVertical();

            Vec3d vel = mc.player.getVelocity();

            double newX, newY, newZ;
            if (separateControl) {
                newX = vel.x * effectiveH;
                newY = effectiveV == 0 ? vel.y : vel.y * effectiveV;
                newZ = vel.z * effectiveH;
            } else {
                // unified control: use horizontal for all axes
                newX = vel.x * effectiveH;
                newY = vel.y * effectiveH;
                newZ = vel.z * effectiveH;
            }

            mc.player.setVelocity(newX, newY, newZ);
        }
    }

    private void handleReduce() {
        if (!wasHit) return;
        if (mc.player.hurtTime > 0) {
            double effectiveH = getEffectiveHorizontal();
            double effectiveV = getEffectiveVertical();

            Vec3d vel = mc.player.getVelocity();

            double newX, newY, newZ;
            if (separateControl) {
                newX = vel.x * effectiveH;
                newY = vel.y * effectiveV;
                newZ = vel.z * effectiveH;
            } else {
                newX = vel.x * effectiveH;
                newY = vel.y * effectiveH;
                newZ = vel.z * effectiveH;
            }

            mc.player.setVelocity(newX, newY, newZ);
        }
        if (mc.player.hurtTime == 0) wasHit = false;
    }

    private void handleGrim() {
        // grim anti-cheat bypass: sprint toggle within the same tick
        // this resets the velocity on the server side
        if (grimTicks > 0) {
            grimTicks--;

            // send sprint stop/start sequence (configurable toggle count)
            for (int i = 0; i < grimSprintToggleCount; i++) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                        mc.player, i % 2 == 0 ?
                                ClientCommandC2SPacket.Mode.START_SPRINTING :
                                ClientCommandC2SPacket.Mode.STOP_SPRINTING
                ));
            }
            // always end sprinting
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING
            ));

            // also send position packet to confirm
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()
            ));
        }

        // zero any remaining velocity from hits
        if (mc.player.hurtTime > 0) {
            double effectiveH = getEffectiveHorizontal();
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * Math.max(effectiveH, 0.6), vel.y, vel.z * Math.max(effectiveH, 0.6));
        }
    }

    private void handleJumpReset() {
        // jump to reset velocity - looks natural
        if (needsJump) {
            if (currentJumpDelay > 0) {
                currentJumpDelay--;
            } else if (mc.player.isOnGround()) {
                mc.player.jump();
                needsJump = false;

                // immediately reduce horizontal velocity
                double effectiveH = getEffectiveHorizontal();
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x * Math.max(effectiveH, 0.4), vel.y, vel.z * Math.max(effectiveH, 0.4));
            }
        }

        if (wasHit && mc.player.hurtTime > 0) {
            double effectiveH = getEffectiveHorizontal();
            Vec3d vel = mc.player.getVelocity();

            if (separateControl) {
                mc.player.setVelocity(
                        vel.x * effectiveH,
                        vel.y * getEffectiveVertical(),
                        vel.z * effectiveH
                );
            } else {
                mc.player.setVelocity(
                        vel.x * effectiveH,
                        vel.y,
                        vel.z * effectiveH
                );
            }
        }
        if (mc.player.hurtTime == 0) wasHit = false;
    }

    private void handleReverse() {
        // reverse knockback: push toward attacker instead of away
        if (!wasHit) return;

        if (mc.player.hurtTime > 0) {
            Vec3d vel = mc.player.getVelocity();

            double newX = vel.x * reverseHorizontal;
            double newY = reverseVertical == 0 ? vel.y : vel.y * reverseVertical;
            double newZ = vel.z * reverseHorizontal;

            mc.player.setVelocity(newX, newY, newZ);
        }

        if (mc.player.hurtTime == 0) wasHit = false;
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }
    public void setHorizontal(double h) { this.horizontal = h; }
    public void setVertical(double v) { this.vertical = v; }
    public void setSeparateControl(boolean val) { this.separateControl = val; }
    public void setChanceBypass(boolean val) { this.chanceBypass = val; }
    public void setLetThroughChance(double chance) { this.letThroughChance = chance; }
    public void setAdaptiveReduction(boolean val) { this.adaptiveReduction = val; }
    public void setReverseHorizontal(double val) { this.reverseHorizontal = val; }
    public void setJumpResetDelay(int ticks) { this.jumpResetDelay = ticks; }
}
