package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class AutoArmor extends Module {

    private int tickDelay = 0;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips the best armor from your inventory", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        // don't spam clicks, add a small delay
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        for (int armorSlot = 0; armorSlot < 4; armorSlot++) {
            ItemStack currentArmor = mc.player.getInventory().getArmorStack(armorSlot);
            int bestSlot = -1;
            int bestProt = currentArmor.getItem() instanceof ArmorItem
                    ? ((ArmorItem) currentArmor.getItem()).getProtection() : -1;

            // scan inventory for better armor
            for (int i = 0; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() instanceof ArmorItem armorItem) {
                    if (armorItem.getSlotType().getEntitySlotId() == armorSlot) {
                        int prot = armorItem.getProtection();
                        if (prot > bestProt) {
                            bestProt = prot;
                            bestSlot = i;
                        }
                    }
                }
            }

            if (bestSlot != -1) {
                int screenSlot = bestSlot < 9 ? bestSlot + 36 : bestSlot;
                // shift-click to equip
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        screenSlot,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );
                tickDelay = 3;
                return; // one piece per cycle
            }
        }
    }
}
