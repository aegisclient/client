package dev.aegis.client.module.combat;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class AutoCrystal extends Module {

    // ranges
    private double placeRange = 5.0;
    private double breakRange = 5.5;
    private double enemyRange = 12.0;

    // damage thresholds
    private double minDamage = 6.0;
    private double maxSelfDamage = 12.0;
    private double facePlaceHP = 8.0;   // lower minDamage threshold when enemy is low HP

    // timing
    private int breakDelay = 0;
    private int placeDelay = 1;
    private int breakTicks = 0;
    private int placeTicks = 0;

    // modes
    private boolean antiSuicide = true;
    private double antiSuicideHP = 4.0;
    private boolean multiPlace = true;  // place multiple crystals per tick
    private boolean predict = true;     // predict crystal placement
    private boolean rotate = true;
    private boolean inhibit = true;     // inhibit crystal break when placing

    private final Random random = new Random();
    private EndCrystalEntity lastBroken = null;
    private BlockPos lastPlacePos = null;
    private PlayerEntity currentTarget = null;

    public AutoCrystal() {
        super("AutoCrystal", "Full crystal PvP automation with damage estimation", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        breakTicks = 0;
        placeTicks = 0;
        lastBroken = null;
        lastPlacePos = null;
        currentTarget = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // anti-suicide check
        if (antiSuicide && mc.player.getHealth() + mc.player.getAbsorptionAmount() <= antiSuicideHP) return;

        currentTarget = findBestEnemy();
        if (currentTarget == null) return;

        // break phase
        if (breakTicks <= 0) {
            breakBestCrystal();
            breakTicks = breakDelay + (random.nextInt(2) == 0 ? 1 : 0);
        } else {
            breakTicks--;
        }

        // place phase
        if (placeTicks <= 0) {
            placeBestCrystals();
            placeTicks = placeDelay + (random.nextInt(3) == 0 ? 1 : 0);
        } else {
            placeTicks--;
        }
    }

    private void breakBestCrystal() {
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(breakRange),
                crystal -> mc.player.distanceTo(crystal) <= breakRange
        );

        // score and sort crystals
        List<CrystalScore> scored = new ArrayList<>();
        for (EndCrystalEntity crystal : crystals) {
            double enemyDamage = calculateDamage(crystal.getPos(), currentTarget);
            double selfDamage = calculateDamage(crystal.getPos(), mc.player);

            if (selfDamage > maxSelfDamage) continue;
            if (antiSuicide && mc.player.getHealth() - selfDamage <= antiSuicideHP) continue;

            // face-place: lower threshold when enemy is low HP
            double threshold = currentTarget.getHealth() <= facePlaceHP ? 2.0 : minDamage;
            if (enemyDamage < threshold) continue;

            double score = enemyDamage - selfDamage * 0.5;
            scored.add(new CrystalScore(crystal, null, score, enemyDamage, selfDamage));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        if (!scored.isEmpty()) {
            EndCrystalEntity best = (EndCrystalEntity) scored.get(0).entity;

            // rotate to crystal
            if (rotate) {
                float[] angles = getRotation(best.getPos());
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        angles[0], angles[1], mc.player.isOnGround()
                ));
            }

            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastBroken = best;
        }
    }

    private void placeBestCrystals() {
        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1) return;

        // search placement positions
        List<CrystalScore> scored = new ArrayList<>();
        BlockPos playerPos = mc.player.getBlockPos();
        int range = (int) Math.ceil(placeRange);

        for (int x = -range; x <= range; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (!canPlaceCrystal(pos)) continue;
                    Vec3d crystalPos = Vec3d.ofCenter(pos).add(0, 1, 0);
                    if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) > placeRange) continue;

                    double enemyDamage = calculateDamage(crystalPos, currentTarget);
                    double selfDamage = calculateDamage(crystalPos, mc.player);

                    if (selfDamage > maxSelfDamage) continue;
                    if (antiSuicide && mc.player.getHealth() - selfDamage <= antiSuicideHP) continue;

                    double threshold = currentTarget.getHealth() <= facePlaceHP ? 2.0 : minDamage;
                    if (enemyDamage < threshold) continue;

                    double score = enemyDamage - selfDamage * 0.5;
                    scored.add(new CrystalScore(null, pos, score, enemyDamage, selfDamage));
                }
            }
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        int placementCount = multiPlace ? Math.min(scored.size(), 2) : Math.min(scored.size(), 1);

        for (int i = 0; i < placementCount; i++) {
            BlockPos pos = scored.get(i).placePos;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;

            // rotate to placement
            if (rotate) {
                float[] angles = getRotation(Vec3d.ofCenter(pos).add(0, 0.5, 0));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        angles[0], angles[1], mc.player.isOnGround()
                ));
            }

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3d.ofCenter(pos).add(0, 0.5, 0),
                    Direction.UP,
                    pos,
                    false
            );
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.getInventory().selectedSlot = prevSlot;
            lastPlacePos = pos;
        }
    }

    private double calculateDamage(Vec3d crystalPos, PlayerEntity target) {
        // full explosion damage calculation matching vanilla
        double dist = target.getPos().distanceTo(crystalPos);
        if (dist > 12.0) return 0;

        // exposure - how much of the entity is exposed to the explosion
        double exposure = getExposure(crystalPos, target);
        double impact = (1.0 - (dist / 12.0)) * exposure;
        double rawDamage = (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;

        // apply armor reduction
        rawDamage = applyArmorReduction(target, rawDamage);

        // apply blast protection
        rawDamage = applyBlastProtection(target, rawDamage);

        // apply resistance effect
        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int level = target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1;
            rawDamage = rawDamage * Math.max(0, 1.0 - level * 0.2);
        }

        return Math.max(0, rawDamage);
    }

    private double getExposure(Vec3d source, PlayerEntity target) {
        Box box = target.getBoundingBox();
        double xStep = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double yStep = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double zStep = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);

        if (xStep <= 0 || yStep <= 0 || zStep <= 0) return 0;

        int total = 0;
        int exposed = 0;

        for (double x = 0; x <= 1.0; x += xStep) {
            for (double y = 0; y <= 1.0; y += yStep) {
                for (double z = 0; z <= 1.0; z += zStep) {
                    double px = box.minX + (box.maxX - box.minX) * x;
                    double py = box.minY + (box.maxY - box.minY) * y;
                    double pz = box.minZ + (box.maxZ - box.minZ) * z;

                    Vec3d point = new Vec3d(px, py, pz);
                    if (mc.world.raycast(new net.minecraft.world.RaycastContext(
                            point, source,
                            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                            net.minecraft.world.RaycastContext.FluidHandling.NONE,
                            target
                    )).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
                        exposed++;
                    }
                    total++;
                }
            }
        }

        return total == 0 ? 0 : (double) exposed / total;
    }

    private double applyArmorReduction(PlayerEntity target, double damage) {
        // calculate armor value
        double armor = target.getArmor();
        double toughness = target.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        return DamageUtil.getDamageLeft((float) damage, (float) armor, (float) toughness);
    }

    private double applyBlastProtection(PlayerEntity target, double damage) {
        int blastProtLevel = 0;
        for (ItemStack stack : target.getArmorItems()) {
            blastProtLevel += EnchantmentHelper.getLevel(Enchantments.BLAST_PROTECTION, stack);
            blastProtLevel += EnchantmentHelper.getLevel(Enchantments.PROTECTION, stack);
        }
        if (blastProtLevel > 0) {
            int capped = Math.min(blastProtLevel, 20);
            damage *= (1.0 - capped * 0.04);
        }
        return damage;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) return false;

        BlockPos above = pos.up();
        if (!mc.world.getBlockState(above).isAir()) return false;
        // 1.20.4: only need 1 air block above for crystal placement
        // but check for entity collision
        Box box = new Box(above);
        return mc.world.getOtherEntities(null, box, e -> !(e instanceof EndCrystalEntity)).isEmpty();
    }

    private PlayerEntity findBestEnemy() {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(PlayerEntity::isAlive)
                .filter(p -> !Aegis.getInstance().getFriendManager().isFriend(p.getGameProfile().getName()))
                .filter(p -> mc.player.distanceTo(p) <= enemyRange)
                .min(Comparator.<PlayerEntity, Double>comparing(p -> (double) p.getHealth())
                        .thenComparing(p -> mc.player.distanceTo(p)))
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

    private float[] getRotation(Vec3d target) {
        double dx = target.x - mc.player.getEyePos().x;
        double dy = target.y - mc.player.getEyePos().y;
        double dz = target.z - mc.player.getEyePos().z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    public PlayerEntity getCurrentTarget() { return currentTarget; }

    private record CrystalScore(Object entity, BlockPos placePos, double score, double enemyDamage, double selfDamage) {}
}
