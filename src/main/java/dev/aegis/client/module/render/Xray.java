package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class Xray extends Module {

    private static final Set<Block> XRAY_BLOCKS = new HashSet<>();

    static {
        // ores
        XRAY_BLOCKS.add(Blocks.DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.IRON_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        XRAY_BLOCKS.add(Blocks.COAL_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        XRAY_BLOCKS.add(Blocks.LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        XRAY_BLOCKS.add(Blocks.ANCIENT_DEBRIS);

        // valuable blocks
        XRAY_BLOCKS.add(Blocks.DIAMOND_BLOCK);
        XRAY_BLOCKS.add(Blocks.EMERALD_BLOCK);
        XRAY_BLOCKS.add(Blocks.GOLD_BLOCK);
        XRAY_BLOCKS.add(Blocks.IRON_BLOCK);
        XRAY_BLOCKS.add(Blocks.NETHERITE_BLOCK);

        // storage & spawners
        XRAY_BLOCKS.add(Blocks.CHEST);
        XRAY_BLOCKS.add(Blocks.ENDER_CHEST);
        XRAY_BLOCKS.add(Blocks.TRAPPED_CHEST);
        XRAY_BLOCKS.add(Blocks.BARREL);
        XRAY_BLOCKS.add(Blocks.SHULKER_BOX);
        XRAY_BLOCKS.add(Blocks.SPAWNER);

        // utility
        XRAY_BLOCKS.add(Blocks.TNT);
        XRAY_BLOCKS.add(Blocks.LAVA);
        XRAY_BLOCKS.add(Blocks.WATER);
        XRAY_BLOCKS.add(Blocks.OBSIDIAN);
        XRAY_BLOCKS.add(Blocks.END_PORTAL_FRAME);
        XRAY_BLOCKS.add(Blocks.BEACON);
    }

    public Xray() {
        super("Xray", "See ores and valuable blocks through walls", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Override
    protected void onDisable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    public static boolean isXrayBlock(Block block) {
        return XRAY_BLOCKS.contains(block);
    }
}
