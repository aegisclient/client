package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class AutoRod extends Module {

    private int delay = 0;
    private double range = 10.0;
    private boolean thrown = false;

    public AutoRod() {
        super("AutoRod", "Automatically throws fishing rod at nearby enemies for knockback", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        // check if holding rod
        int rodSlot = findRodSlot();
        if (rodSlot == -1) return;

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.isAlive())
                .filter(p -> mc.player.distanceTo(p) <= range)
                .findFirst()
                .orElse(null);

        if (target == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = rodSlot;

        if (thrown) {
            // reel in
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            thrown = false;
            delay = 5;
            mc.player.getInventory().selectedSlot = prevSlot;
        } else {
            // throw
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            thrown = true;
            delay = 10;
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private int findRodSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FISHING_ROD) {
                return i;
            }
        }
        return -1;
    }
}
