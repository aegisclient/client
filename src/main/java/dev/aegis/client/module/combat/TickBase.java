package dev.aegis.client.module.combat;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class TickBase extends Module {

    private int ticksToShift = 5;
    private double activateRange = 4.0;
    private boolean charging = false;
    private int chargedTicks = 0;

    public TickBase() {
        super("TickBase", "Manipulates client tick timing to gain advantage in combat", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(LivingEntity::isAlive)
                .filter(p -> mc.player.distanceTo(p) <= activateRange + 2)
                .findFirst()
                .orElse(null);

        if (target == null) {
            charging = false;
            chargedTicks = 0;
            return;
        }

        double dist = mc.player.distanceTo(target);

        if (dist > activateRange && !charging) {
            // start charging ticks (freeze client movement)
            charging = true;
            chargedTicks = 0;
        }

        if (charging) {
            chargedTicks++;
            if (chargedTicks >= ticksToShift || dist <= activateRange) {
                // release all charged ticks at once
                for (int i = 0; i < chargedTicks; i++) {
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();
                    mc.player.networkHandler.sendPacket(
                            new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround())
                    );
                }
                charging = false;
                chargedTicks = 0;
            }
        }
    }
}
