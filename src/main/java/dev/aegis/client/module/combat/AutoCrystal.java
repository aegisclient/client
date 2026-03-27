package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class AutoCrystal extends Module {

    private double placeRange = 5.0;
    private double breakRange = 5.5;
    private double minDamage = 6.0;
    private double maxSelfDamage = 10.0;
    private int breakDelay = 0;
    private int placeDelay = 0;
    private int breakTicks = 0;
    private int placeTicks = 0;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks end crystals for PvP", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // break existing crystals first
        if (breakTicks <= 0) {
            breakNearestCrystal();
            breakTicks = breakDelay;
        } else {
            breakTicks--;
        }

        // then place new ones
        if (placeTicks <= 0) {
            placeBestCrystal();
            placeTicks = placeDelay;
        } else {
            placeTicks--;
        }
    }

    private void breakNearestCrystal() {
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(breakRange),
                crystal -> mc.player.distanceTo(crystal) <= breakRange
        );

        // break the crystal closest to enemy players for max damage
        EndCrystalEntity bestCrystal = null;
        double bestScore = 0;

        for (EndCrystalEntity crystal : crystals) {
            // check if breaking this crystal would hit an enemy
            PlayerEntity nearestEnemy = findNearestEnemy(crystal.getPos(), 12.0);
            if (nearestEnemy == null) continue;

            double enemyDist = nearestEnemy.squaredDistanceTo(crystal);
            double selfDist = mc.player.squaredDistanceTo(crystal);

            // skip if we'd take too much damage
            double selfDamage = estimateCrystalDamage(selfDist);
            if (selfDamage > maxSelfDamage) continue;

            double enemyDamage = estimateCrystalDamage(enemyDist);
            if (enemyDamage < minDamage) continue;

            double score = enemyDamage - selfDamage;
            if (score > bestScore) {
                bestScore = score;
                bestCrystal = crystal;
            }
        }

        if (bestCrystal != null) {
            mc.interactionManager.attackEntity(mc.player, bestCrystal);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void placeBestCrystal() {
        // need end crystal in hand or hotbar
        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1) return;

        PlayerEntity target = findNearestEnemy(mc.player.getPos(), placeRange + 6);
        if (target == null) return;

        BlockPos bestPos = null;
        double bestDamage = minDamage;

        // search for valid placement positions
        BlockPos playerPos = mc.player.getBlockPos();
        int range = (int) Math.ceil(placeRange);

        for (int x = -range; x <= range; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (!canPlaceCrystal(pos)) continue;
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > placeRange) continue;

                    // calculate damage to target
                    double enemyDist = target.getPos().squaredDistanceTo(Vec3d.ofCenter(pos.up()));
                    double enemyDamage = estimateCrystalDamage(enemyDist);

                    double selfDist = mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(pos.up()));
                    double selfDamage = estimateCrystalDamage(selfDist);

                    if (selfDamage > maxSelfDamage) continue;

                    double score = enemyDamage - selfDamage;
                    if (score > bestDamage) {
                        bestDamage = score;
                        bestPos = pos;
                    }
                }
            }
        }

        if (bestPos != null) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3d.ofCenter(bestPos),
                    Direction.UP,
                    bestPos,
                    false
            );
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) return false;

        BlockPos above = pos.up();
        if (!mc.world.getBlockState(above).isAir()) return false;
        if (!mc.world.getBlockState(above.up()).isAir()) return false;

        // check no entities in the way
        Box box = new Box(above);
        return mc.world.getOtherEntities(null, box).isEmpty();
    }

    private PlayerEntity findNearestEnemy(Vec3d pos, double range) {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.isAlive())
                .filter(p -> p.distanceTo(mc.player) <= range)
                .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(pos)))
                .orElse(null);
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                return i;
            }
        }
        return -1;
    }

    private double estimateCrystalDamage(double squaredDistance) {
        // simplified crystal damage formula
        double dist = Math.sqrt(squaredDistance);
        if (dist > 12) return 0;
        double impact = (1.0 - dist / 12.0);
        return impact * impact * 42.0; // rough approximation
    }
}
