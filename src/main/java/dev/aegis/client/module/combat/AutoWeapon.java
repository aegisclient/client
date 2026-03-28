package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import org.lwjgl.glfw.GLFW;

public class AutoWeapon extends Module {

    public AutoWeapon() {
        super("AutoWeapon", "Automatically switches to the best weapon before attacking", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // weapon switch is triggered before attacks by combat modules
    }

    public void switchToBestWeapon() {
        if (mc.player == null) return;

        int bestSlot = -1;
        double bestDamage = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
                double damage = getItemAttackDamage(stack);
                // prefer swords slightly
                if (stack.getItem() instanceof SwordItem) damage += 0.5;
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private double getItemAttackDamage(ItemStack stack) {
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        var damageModifiers = modifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double damage = 1.0;
        for (var modifier : damageModifiers) {
            damage += modifier.getValue();
        }
        return damage;
    }
}
