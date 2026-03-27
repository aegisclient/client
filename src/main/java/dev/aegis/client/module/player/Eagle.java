package dev.aegis.client.module.player;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class Eagle extends Module {

    public Eagle() {
        super("Eagle", "Auto-sneak at block edges for bridging", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean airBelow = mc.world.getBlockState(below).isAir();

        if (airBelow && mc.player.isOnGround()) {
            mc.player.setSneaking(true);
        } else {
            if (!mc.options.sneakKey.isPressed()) {
                mc.player.setSneaking(false);
            }
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && !mc.options.sneakKey.isPressed()) {
            mc.player.setSneaking(false);
        }
    }
}
