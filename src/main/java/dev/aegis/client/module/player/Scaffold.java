package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;
import java.util.Set;

public class Scaffold extends Module {

    // modes
    private boolean tower = true;        // place while jumping straight up
    private boolean extend = true;       // extend placement reach for diagonal bridging
    private int extendDistance = 3;       // how many blocks ahead to extend
    private boolean rotate = true;       // server-side rotation to placement
    private boolean safeWalk = true;     // prevent falling off edges
    private boolean autoSwitch = true;   // auto-switch to block slot
    private boolean noSwing = false;     // hide swing animation

    // rotation simulation
    private boolean smoothRotation = true;       // gradual rotation to placement
    private float rotationSpeed = 60.0f;         // degrees per tick for smooth rotation
    private float currentScaffYaw;
    private float currentScaffPitch;
    private boolean rotationInitialized = false;

    // tower mode enhancements
    private double towerSpeed = 0.42;            // tower jump velocity
    private boolean towerMotionReset = true;     // reset horizontal motion during tower
    private int towerPlaceDelay = 0;             // delay between tower placements

    // keep Y mode: maintain starting Y level
    private boolean keepY = false;
    private int keepYLevel = -1;                 // -1 = not set yet

    // anti-cheat
    private boolean eagle = true;        // auto-sneak at edges
    private int eagleTicks = 2;          // sneak for this many ticks at edge
    private boolean randomize = true;    // randomize placement timing

    // block placement delay randomization
    private int minPlaceDelay = 0;       // minimum ticks between placements
    private int maxPlaceDelay = 2;       // maximum ticks between placements
    private boolean placeJitter = true;  // add ms-level jitter to placement timing
    private long lastPlaceTimeMs = 0;
    private int placeJitterMs = 30;      // +/- ms jitter

    private int sneakTicks = 0;
    private int placeDelay = 0;
    private int towerDelay = 0;
    private BlockPos lastPlacement = null;
    private int blocksPlaced = 0;
    private final Random random = new Random();

    // blacklist: blocks that shouldn't be used for scaffolding
    private static final Set<Block> BLACKLIST = Set.of(
            Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.NETHERITE_BLOCK,
            Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK, Blocks.TNT, Blocks.SAND,
            Blocks.GRAVEL, Blocks.ANVIL, Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.CRAFTING_TABLE, Blocks.FURNACE
    );

    public Scaffold() {
        super("Scaffold", "Auto-place blocks under you while walking", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        sneakTicks = 0;
        placeDelay = 0;
        towerDelay = 0;
        lastPlacement = null;
        blocksPlaced = 0;
        rotationInitialized = false;
        lastPlaceTimeMs = 0;
        if (mc.player != null) {
            keepYLevel = keepY ? mc.player.getBlockPos().getY() - 1 : -1;
            currentScaffYaw = mc.player.getYaw();
            currentScaffPitch = mc.player.getPitch();
        }
    }

    @Override
    protected void onDisable() {
        // stop sneaking if we were
        if (mc.player != null && sneakTicks > 0) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY
            ));
        }
        keepYLevel = -1;
        rotationInitialized = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // handle placement delay
        if (placeDelay > 0) {
            placeDelay--;
            return;
        }

        // ms-level jitter check
        if (placeJitter) {
            long now = System.currentTimeMillis();
            if (lastPlaceTimeMs > 0) {
                long elapsed = now - lastPlaceTimeMs;
                int jitteredMin = 40 + random.nextInt(placeJitterMs * 2 + 1) - placeJitterMs;
                if (elapsed < Math.max(jitteredMin, 0)) return;
            }
        }

        if (towerDelay > 0) {
            towerDelay--;
        }

        // find block slot
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        // safe-walk edge detection
        if (safeWalk || eagle) {
            handleEdgeSneak();
        }

        // determine target Y for placement
        int targetY;
        if (keepY && keepYLevel != -1) {
            targetY = keepYLevel;
        } else {
            targetY = mc.player.getBlockPos().getY() - 1;
        }

        // tower mode: place below while jumping straight up
        if (tower && mc.options.jumpKey.isPressed() && mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) {
            handleTower(blockSlot);
            return;
        }

        // regular scaffold: place below current position
        BlockPos below = new BlockPos(mc.player.getBlockPos().getX(), targetY, mc.player.getBlockPos().getZ());
        if (placeBlockAt(below, blockSlot)) {
            setPlaceDelay();
            return;
        }

        // extend: try placing ahead of the player
        if (extend) {
            handleExtend(blockSlot, targetY);
        }
    }

    private void setPlaceDelay() {
        if (randomize) {
            placeDelay = minPlaceDelay + random.nextInt(maxPlaceDelay - minPlaceDelay + 1);
        } else {
            placeDelay = minPlaceDelay;
        }
        lastPlaceTimeMs = System.currentTimeMillis();
    }

    private void handleTower(int blockSlot) {
        BlockPos below = mc.player.getBlockPos().down();

        if (mc.world.getBlockState(below).isAir()) {
            if (towerDelay <= 0 && placeBlockAt(below, blockSlot)) {
                // tower jump - set velocity to go up
                double hMotion = towerMotionReset ? 0.0 : mc.player.getVelocity().x * 0.3;
                double hMotionZ = towerMotionReset ? 0.0 : mc.player.getVelocity().z * 0.3;
                mc.player.setVelocity(hMotion, towerSpeed, hMotionZ);
                mc.player.jump();
                towerDelay = towerPlaceDelay;
                blocksPlaced++;
            }
        } else if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private void handleExtend(int blockSlot, int targetY) {
        // get movement direction
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0) return;

        double motionX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double motionZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;
        double mag = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (mag < 0.01) return;
        motionX /= mag;
        motionZ /= mag;

        // try placing blocks ahead
        BlockPos playerBelow = new BlockPos(mc.player.getBlockPos().getX(), targetY, mc.player.getBlockPos().getZ());
        for (int i = 1; i <= extendDistance; i++) {
            BlockPos ahead = playerBelow.add(
                    (int) Math.round(motionX * i), 0, (int) Math.round(motionZ * i)
            );
            if (mc.world.getBlockState(ahead).isAir()) {
                if (placeBlockAt(ahead, blockSlot)) {
                    setPlaceDelay();
                    return;
                }
            }
        }
    }

    private void handleEdgeSneak() {
        BlockPos below = mc.player.getBlockPos().down();
        boolean atEdge = mc.world.getBlockState(below).isAir();

        if (atEdge && eagle) {
            if (sneakTicks == 0) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                ));
            }
            sneakTicks = eagleTicks;
        }

        if (sneakTicks > 0) {
            sneakTicks--;
            if (sneakTicks == 0) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY
                ));
            }
        }
    }

    private boolean placeBlockAt(BlockPos pos, int blockSlot) {
        if (!mc.world.getBlockState(pos).isAir()) return false;

        // find a face to place against
        PlaceData placeData = findPlacement(pos);
        if (placeData == null) return false;

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch) {
            mc.player.getInventory().selectedSlot = blockSlot;
        }

        // server-side rotation simulation
        if (rotate) {
            float[] angles = getPlacementRotation(placeData.hitVec);

            if (smoothRotation) {
                // gradually step toward target rotation instead of snapping
                float yawDiff = MathHelper.wrapDegrees(angles[0] - currentScaffYaw);
                float pitchDiff = angles[1] - currentScaffPitch;

                float maxStep = rotationSpeed + (random.nextFloat() - 0.5f) * 15.0f;
                float yawStep = MathHelper.clamp(yawDiff, -maxStep, maxStep);
                float pitchStep = MathHelper.clamp(pitchDiff, -maxStep * 0.8f, maxStep * 0.8f);

                // add micro-jitter for humanization
                yawStep += (random.nextFloat() - 0.5f) * 1.2f;
                pitchStep += (random.nextFloat() - 0.5f) * 0.6f;

                currentScaffYaw = MathHelper.wrapDegrees(currentScaffYaw + yawStep);
                currentScaffPitch = MathHelper.clamp(currentScaffPitch + pitchStep, -90f, 90f);

                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        currentScaffYaw, currentScaffPitch, mc.player.isOnGround()
                ));
            } else {
                // instant rotation (original behavior)
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        angles[0], angles[1], mc.player.isOnGround()
                ));
            }
        }

        BlockHitResult hitResult = new BlockHitResult(
                placeData.hitVec, placeData.side, placeData.neighbor, false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

        if (!noSwing) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (autoSwitch) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }

        lastPlacement = pos;
        blocksPlaced++;
        return true;
    }

    private PlaceData findPlacement(BlockPos pos) {
        // prioritize DOWN first (most natural for bridging)
        Direction[] priorities = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};

        for (Direction dir : priorities) {
            BlockPos neighbor = pos.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (!mc.world.getBlockState(neighbor).isSolidBlock(mc.world, neighbor)) continue;

            Direction placeSide = dir.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighbor).add(
                    placeSide.getOffsetX() * 0.5,
                    placeSide.getOffsetY() * 0.5,
                    placeSide.getOffsetZ() * 0.5
            );

            // add slight randomization to hit vector for humanization
            if (randomize) {
                hitVec = hitVec.add(
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1,
                        (random.nextDouble() - 0.5) * 0.1
                );
            }

            return new PlaceData(neighbor, placeSide, hitVec);
        }
        return null;
    }

    private float[] getPlacementRotation(Vec3d target) {
        double dx = target.x - mc.player.getEyePos().x;
        double dy = target.y - mc.player.getEyePos().y;
        double dz = target.z - mc.player.getEyePos().z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        // add slight randomization for human-like aiming
        if (randomize) {
            yaw += (random.nextFloat() - 0.5f) * 2.0f;
            pitch += (random.nextFloat() - 0.5f) * 1.0f;
        }

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
    }

    private int findBlockSlot() {
        // first pass: prefer common building blocks
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (BLACKLIST.contains(block)) continue;
                if (block instanceof FallingBlock) continue;
                if (mc.player.getInventory().getStack(i).getCount() < 2) continue; // save last block
                return i;
            }
        }
        // second pass: any block at all
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (BLACKLIST.contains(block)) continue;
                return i;
            }
        }
        return -1;
    }

    private record PlaceData(BlockPos neighbor, Direction side, Vec3d hitVec) {}

    // setters
    public void setTower(boolean val) { this.tower = val; }
    public void setExtend(boolean val) { this.extend = val; }
    public void setExtendDistance(int dist) { this.extendDistance = dist; }
    public void setKeepY(boolean val) { this.keepY = val; }
    public void setSmoothRotation(boolean val) { this.smoothRotation = val; }
    public void setTowerSpeed(double speed) { this.towerSpeed = speed; }
    public void setMinPlaceDelay(int delay) { this.minPlaceDelay = delay; }
    public void setMaxPlaceDelay(int delay) { this.maxPlaceDelay = delay; }
    public void setPlaceJitterMs(int ms) { this.placeJitterMs = ms; }
}
