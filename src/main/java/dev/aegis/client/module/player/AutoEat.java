package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class AutoEat extends Module {

    private int hungerThreshold = 14;
    private boolean eating = false;
    private int prevSlot = -1;

    // preferred foods ranked by saturation
    private static final List<Item> GOOD_FOODS = Arrays.asList(
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_APPLE,
            Items.GOLDEN_CARROT,
            Items.COOKED_BEEF,
            Items.COOKED_PORKCHOP,
            Items.COOKED_MUTTON,
            Items.COOKED_SALMON,
            Items.COOKED_CHICKEN,
            Items.BREAD,
            Items.BAKED_POTATO,
            Items.COOKED_COD,
            Items.APPLE,
            Items.CARROT
    );

    public AutoEat() {
        super("AutoEat", "Automatically eats food when hungry", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        int foodLevel = mc.player.getHungerManager().getFoodLevel();

        // stop eating if full
        if (eating && foodLevel >= 20) {
            stopEating();
            return;
        }

        if (foodLevel > hungerThreshold) return;

        // find the best food in hotbar
        int bestSlot = -1;
        int bestRank = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            int rank = GOOD_FOODS.indexOf(item);
            if (rank != -1 && rank < bestRank) {
                bestRank = rank;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        // switch to food and eat
        if (!eating) {
            prevSlot = mc.player.getInventory().selectedSlot;
            eating = true;
        }

        mc.player.getInventory().selectedSlot = bestSlot;
        mc.options.useKey.setPressed(true);
    }

    private void stopEating() {
        eating = false;
        mc.options.useKey.setPressed(false);
        if (prevSlot != -1) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @Override
    protected void onDisable() {
        stopEating();
    }
}
