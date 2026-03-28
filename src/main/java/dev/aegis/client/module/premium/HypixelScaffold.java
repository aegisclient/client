package dev.aegis.client.module.premium;

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

public class HypixelScaffold extends Module {

    // Watchdog-safe scaffold parameters
    private boolean tower = true;
    private boolean rotate = true;
    private boolean eagle = true;
    private boolean autoSwitch = true;

    // tower with timer manipulation
    private static final double TOWER_MOTION_Y = 0.42;
    private static final int TOWER_TIMER_BOOST = 2; // slight timer acceleration during tower

    // rotation smoothing
    private float lastRotYaw = 0;
    private float lastRotPitch = 0;
    private float targetYaw = 0;
    private float targetPitch = 0;
    private static final float ROTATION_SPEED = 35.0f; // degrees per tick max rotation
    private static final float ROTATION_JITTER = 2.5f; // humanization jitter

    // state
    private int placeDelay = 0;
    private int sneakTicks = 0;
    private int towerTicks = 0;
    private BlockPos lastPlacement = null;
    private int tickCounter = 0;

    private final Random random = new Random();

    private static final Set<Block> BLACKLIST = Set.of(
            Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.NETHERITE_BLOCK,
            Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK, Blocks.TNT, Blocks.SAND,
            Blocks.GRAVEL, Blocks.ANVIL, Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.CRAFTING_TABLE, Blocks.FURNACE
    );

    public HypixelScaffold() {
        super("HypixelScaffold", "Hypixel-safe scaffold with tower timer manipulation and rotation smoothing", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        placeDelay = 0;
        sneakTicks = 0;
        towerTicks = 0;
        lastPlacement = null;
        tickCounter = 0;
        if (mc.player != null) {
            lastRotYaw = mc.player.getYaw();
            lastRotPitch = mc.player.getPitch();
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && sneakTicks > 0) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY
            ));
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        tickCounter++;

        if (placeDelay > 0) {
            placeDelay--;
            return;
        }

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        // eagle at edges for Watchdog
        if (eagle) {
            handleEdgeSneak();
        }

        // tower mode
        if (tower && mc.options.jumpKey.isPressed() &&
                mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) {
            handleTower(blockSlot);
            return;
        }

        // regular scaffold placement
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            if (placeBlockAt(below, blockSlot)) {
                // randomized delay to mimic human timing (Watchdog checks placement intervals)
                placeDelay = random.nextInt(2);
            }
        }
    }

    private void handleTower(int blockSlot) {
        towerTicks++;

        BlockPos below = mc.player.getBlockPos().down();

        if (mc.world.getBlockState(below).isAir()) {
            if (placeBlockAt(below, blockSlot)) {
                // tower jump with slight horizontal dampening
                mc.player.setVelocity(
                        mc.player.getVelocity().x * 0.2,
                        TOWER_MOTION_Y,
                        mc.player.getVelocity().z * 0.2
                );
                mc.player.jump();

                // timer manipulation: send extra position packets to speed up tower
                // Watchdog allows slight timer variance
                if (towerTicks % TOWER_TIMER_BOOST == 0) {
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.001, z, true
                    ));
                }
            }
        } else if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private void handleEdgeSneak() {
        BlockPos below = mc.player.getBlockPos().down();
        boolean atEdge = mc.world.getBlockState(below).isAir();

        if (atEdge) {
            if (sneakTicks == 0) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                ));
            }
            sneakTicks = 2;
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

        PlaceData placeData = findPlacement(pos);
        if (placeData == null) return false;

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch) {
            mc.player.getInventory().selectedSlot = blockSlot;
        }

        // smooth rotation to placement target
        if (rotate) {
            float[] targetAngles = getPlacementRotation(placeData.hitVec);
            targetYaw = targetAngles[0];
            targetPitch = targetAngles[1];

            // smooth interpolation with jitter
            float smoothYaw = smoothRotate(lastRotYaw, targetYaw, ROTATION_SPEED);
            float smoothPitch = smoothRotate(lastRotPitch, targetPitch, ROTATION_SPEED);

            // add human-like micro-jitter
            smoothYaw += (random.nextFloat() - 0.5f) * ROTATION_JITTER;
            smoothPitch += (random.nextFloat() - 0.5f) * (ROTATION_JITTER * 0.5f);

            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    smoothYaw, smoothPitch, mc.player.isOnGround()
            ));

            lastRotYaw = smoothYaw;
            lastRotPitch = smoothPitch;
        }

        BlockHitResult hitResult = new BlockHitResult(
                placeData.hitVec, placeData.side, placeData.neighbor, false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (autoSwitch) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }

        lastPlacement = pos;
        return true;
    }

    private float smoothRotate(float current, float target, float maxSpeed) {
        float diff = MathHelper.wrapDegrees(target - current);
        float clampedSpeed = maxSpeed + random.nextFloat() * 5.0f;
        if (Math.abs(diff) <= clampedSpeed) return target;
        return current + Math.signum(diff) * clampedSpeed;
    }

    private PlaceData findPlacement(BlockPos pos) {
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
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f)};
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (BLACKLIST.contains(block)) continue;
                if (block instanceof FallingBlock) continue;
                if (mc.player.getInventory().getStack(i).getCount() < 2) continue;
                return i;
            }
        }
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
}
