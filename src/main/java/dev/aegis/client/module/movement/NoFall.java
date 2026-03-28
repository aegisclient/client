package dev.aegis.client.module.movement;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class NoFall extends Module {

    public enum Mode { PACKET, BUCKET, MLG, SMART }

    private Mode mode = Mode.PACKET;
    private int packetCount = 0;
    private final Random random = new Random();

    // packet mode settings
    private int packetStartDistance = 2;        // fall distance before sending packets
    private boolean alternatePackets = true;    // alternate between ground-only and position packets
    private boolean smartTiming = true;         // only send packets at optimal intervals
    private int packetInterval = 2;             // ticks between packets (when smartTiming)

    // bucket/MLG mode settings
    private double mlgActivateDistance = 3.5;   // fall distance to start MLG sequence
    private int mlgGroundDistance = 8;          // blocks above ground to trigger MLG
    private boolean mlgReturnSlot = true;       // return to previous hotbar slot after MLG

    // smart mode: automatically pick best strategy based on context
    private boolean smartAutoSwitch = true;     // auto switch between packet and MLG based on distance

    // anti-detection
    private boolean groundStateSpoof = true;    // send ground=true in alternating pattern
    private boolean positionReinforce = true;   // send position packets to reinforce ground state
    private int reinforceInterval = 6;          // ticks between reinforcement packets

    // state tracking
    private boolean mlgActive = false;
    private int previousSlot = -1;
    private int ticksSinceLastPacket = 0;
    private double lastFallDistance = 0;

    public NoFall() {
        super("NoFall", "Prevents fall damage with multiple bypass modes", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        packetCount = 0;
        mlgActive = false;
        previousSlot = -1;
        ticksSinceLastPacket = 0;
        lastFallDistance = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        switch (mode) {
            case PACKET -> doPacket();
            case BUCKET -> doBucket();
            case MLG -> doMLG();
            case SMART -> doSmart();
        }

        lastFallDistance = mc.player.fallDistance;
    }

    private void doPacket() {
        ticksSinceLastPacket++;

        // only send packets when actually falling significant distance
        if (mc.player.fallDistance > packetStartDistance) {
            packetCount++;

            boolean shouldSend;
            if (smartTiming) {
                // smart timing: vary the interval slightly to avoid pattern detection
                int effectiveInterval = packetInterval;
                if (random.nextInt(4) == 0) {
                    effectiveInterval += random.nextInt(2); // occasionally skip a tick
                }
                shouldSend = ticksSinceLastPacket >= effectiveInterval;
            } else {
                // don't spam every tick - send every other tick to look natural
                shouldSend = packetCount % 2 == 0;
            }

            if (shouldSend) {
                ticksSinceLastPacket = 0;

                if (alternatePackets) {
                    // alternate between ground-only and position packets for anti-detection
                    if (packetCount % 3 == 0) {
                        // send position packet with ground=true
                        double x = mc.player.getX();
                        double y = mc.player.getY();
                        double z = mc.player.getZ();
                        mc.player.networkHandler.sendPacket(
                                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                        );
                    } else if (packetCount % 3 == 1) {
                        // send ground-only packet
                        mc.player.networkHandler.sendPacket(
                                new PlayerMoveC2SPacket.OnGroundOnly(true)
                        );
                    } else {
                        // send full packet with slight position noise
                        double x = mc.player.getX() + (random.nextDouble() - 0.5) * 0.0001;
                        double y = mc.player.getY();
                        double z = mc.player.getZ() + (random.nextDouble() - 0.5) * 0.0001;
                        mc.player.networkHandler.sendPacket(
                                new PlayerMoveC2SPacket.Full(
                                        x, y, z,
                                        mc.player.getYaw(), mc.player.getPitch(), true
                                )
                        );
                    }
                } else {
                    // simple ground-only packet
                    mc.player.networkHandler.sendPacket(
                            new PlayerMoveC2SPacket.OnGroundOnly(true)
                    );
                }
            }

            // periodic position reinforcement
            if (positionReinforce && packetCount % reinforceInterval == 0) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                );
            }
        } else {
            packetCount = 0;
            ticksSinceLastPacket = 0;
        }
    }

    private void doBucket() {
        // water bucket clutch: place water when close to ground
        if (mc.player.fallDistance < mlgActivateDistance) return;

        int waterSlot = findItemSlot(Items.WATER_BUCKET);
        if (waterSlot == -1) {
            // fallback to packet mode if no water bucket
            doPacket();
            return;
        }

        // check distance to ground
        int groundDist = getDistanceToGround();
        if (groundDist <= mlgGroundDistance && groundDist > 0) {
            // switch to water bucket and use it
            previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = waterSlot;

            // look straight down
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    mc.player.getYaw(), 90.0f, false
            ));

            // use item (place water)
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            mlgActive = true;
        }

        // pick up water after landing
        if (mlgActive && mc.player.isOnGround()) {
            // look down and pick up water
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    mc.player.getYaw(), 90.0f, true
            ));
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            if (mlgReturnSlot && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
            }
            mlgActive = false;
            previousSlot = -1;
        }
    }

    private void doMLG() {
        // MLG mode: supports water bucket, hay bale, slime block, etc.
        if (mc.player.fallDistance < mlgActivateDistance) return;

        int groundDist = getDistanceToGround();
        if (groundDist > mlgGroundDistance || groundDist <= 0) return;

        // priority: water bucket > hay bale placement > cobweb > slime block
        int waterSlot = findItemSlot(Items.WATER_BUCKET);
        if (waterSlot != -1) {
            executeBucketMLG(waterSlot);
            return;
        }

        // try block-based MLG (hay bale, slime block)
        int haySlot = findBlockSlot(Blocks.HAY_BLOCK);
        if (haySlot == -1) haySlot = findBlockSlot(Blocks.SLIME_BLOCK);
        if (haySlot != -1 && groundDist <= 4) {
            executeBlockMLG(haySlot);
            return;
        }

        // fallback to packet mode
        doPacket();
    }

    private void doSmart() {
        // smart mode: pick the best strategy based on current context
        if (mc.player.fallDistance < packetStartDistance) {
            packetCount = 0;
            return;
        }

        // if fall distance is small, just use packets
        if (mc.player.fallDistance < 6.0f) {
            doPacket();
            return;
        }

        // if we have a water bucket and falling far, try MLG
        if (smartAutoSwitch) {
            int waterSlot = findItemSlot(Items.WATER_BUCKET);
            if (waterSlot != -1) {
                int groundDist = getDistanceToGround();
                if (groundDist <= mlgGroundDistance && groundDist > 0) {
                    executeBucketMLG(waterSlot);
                    return;
                }
            }
        }

        // default to packet mode with position reinforcement
        doPacket();

        // extra: if falling very far, send more aggressive ground spoofing
        if (mc.player.fallDistance > 15.0f && groundStateSpoof) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.OnGroundOnly(true)
            );
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
                    )
            );
        }
    }

    private void executeBucketMLG(int waterSlot) {
        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = waterSlot;

        // look straight down
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                mc.player.getYaw(), 90.0f, false
        ));

        // use item
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mlgActive = true;

        // schedule pickup on landing
        if (mc.player.isOnGround()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            if (mlgReturnSlot && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
            }
            mlgActive = false;
        }
    }

    private void executeBlockMLG(int blockSlot) {
        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        // look straight down
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                mc.player.getYaw(), 90.0f, false
        ));

        // place block below
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            // find placement surface
            for (int dy = 1; dy <= 3; dy++) {
                BlockPos target = below.down(dy);
                if (!mc.world.getBlockState(target).isAir()) {
                    // place on top of this block
                    Vec3d hitVec = Vec3d.ofCenter(target).add(0, 0.5, 0);
                    net.minecraft.util.hit.BlockHitResult hitResult = new net.minecraft.util.hit.BlockHitResult(
                            hitVec, net.minecraft.util.math.Direction.UP, target, false
                    );
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                    break;
                }
            }
        }

        if (mlgReturnSlot && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
    }

    private int getDistanceToGround() {
        BlockPos pos = mc.player.getBlockPos();
        for (int i = 0; i < 64; i++) {
            BlockPos check = pos.down(i);
            if (!mc.world.getBlockState(check).isAir()) {
                return i;
            }
            if (check.getY() <= mc.world.getBottomY()) return -1;
        }
        return -1;
    }

    private int findItemSlot(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private int findBlockSlot(net.minecraft.block.Block block) {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                if (blockItem.getBlock() == block) return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        packetCount = 0;
        mlgActive = false;
        ticksSinceLastPacket = 0;
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }
    public void setSmartTiming(boolean val) { this.smartTiming = val; }
    public void setPacketInterval(int interval) { this.packetInterval = interval; }
    public void setMlgActivateDistance(double dist) { this.mlgActivateDistance = dist; }
    public void setMlgGroundDistance(int dist) { this.mlgGroundDistance = dist; }
}
