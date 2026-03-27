package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AntiAFK extends Module {

    private int interval = 200; // ticks between actions
    private int ticks = 0;
    private final Random random = new Random();

    public AntiAFK() {
        super("AntiAFK", "Prevents AFK kick by performing random actions periodically", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        ticks++;
        if (ticks < interval) return;
        ticks = 0;

        // randomly pick an action
        int action = random.nextInt(4);

        switch (action) {
            case 0 -> {
                // spin around
                mc.player.setYaw(mc.player.getYaw() + random.nextFloat() * 60 - 30);
            }
            case 1 -> {
                // jump
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
            case 2 -> {
                // swing arm
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }
            case 3 -> {
                // sneak briefly
                mc.player.setSneaking(true);
                // will be unset next action cycle
            }
        }

        // un-sneak
        if (mc.player.isSneaking() && action != 3) {
            mc.player.setSneaking(false);
        }
    }

    @Override
    protected void onDisable() {
        ticks = 0;
        if (mc.player != null) {
            mc.player.setSneaking(false);
        }
    }
}
