package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class Offhand extends Module {

    private double totemHealth = 14.0;
    private int delay = 0;

    public Offhand() {
        super("Offhand", "Smart offhand management - totem when low, crystal/gap when safe", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        ItemStack offhand = mc.player.getOffHandStack();
        boolean lowHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= totemHealth;

        if (lowHealth) {
            // need totem in offhand
            if (offhand.getItem() != Items.TOTEM_OF_UNDYING) {
                int totemSlot = findItem(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1) {
                    swapToOffhand(totemSlot);
                    delay = 2;
                }
            }
        } else {
            // put crystal or gap in offhand for combat
            if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
                int crystalSlot = findItem(Items.END_CRYSTAL);
                if (crystalSlot != -1) {
                    swapToOffhand(crystalSlot);
                    delay = 2;
                }
            }
        }
    }

    private int findItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    private void swapToOffhand(int slot) {
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot, 0, SlotActionType.PICKUP, mc.player
        );
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                45, 0, SlotActionType.PICKUP, mc.player
        );
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot, 0, SlotActionType.PICKUP, mc.player
        );
    }
}
