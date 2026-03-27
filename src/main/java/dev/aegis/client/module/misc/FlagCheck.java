package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FlagCheck extends Module {

    private Vec3d lastPos;
    private int rubberBandCount = 0;
    private int lagbackCount = 0;

    public FlagCheck() {
        super("FlagCheck", "Detects when you're being flagged by anti-cheat (rubber-banding, lagbacks)", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Vec3d currentPos = mc.player.getPos();

        if (lastPos != null) {
            double dist = lastPos.distanceTo(currentPos);

            // detect rubber-banding (teleported back by server)
            if (dist > 5.0 && mc.player.age > 20) {
                lagbackCount++;
                ChatHelper.info("\u00a7c[FlagCheck] \u00a7fLagback detected! (#" + lagbackCount + ") " +
                        String.format("%.1f blocks", dist));
            }
        }

        lastPos = currentPos;
    }

    @Override
    protected void onDisable() {
        lagbackCount = 0;
        rubberBandCount = 0;
        lastPos = null;
    }
}
