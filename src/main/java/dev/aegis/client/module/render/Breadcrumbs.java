package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Breadcrumbs extends Module {

    private final List<Vec3d> positions = new ArrayList<>();
    private int maxPositions = 2000;

    public Breadcrumbs() {
        super("Breadcrumbs", "Draws a trail behind you showing where you've been", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Vec3d pos = mc.player.getPos();
        if (positions.isEmpty() || positions.get(positions.size() - 1).distanceTo(pos) > 0.5) {
            positions.add(pos);
        }

        while (positions.size() > maxPositions) {
            positions.remove(0);
        }
    }

    public List<Vec3d> getPositions() {
        return positions;
    }

    @Override
    protected void onDisable() {
        positions.clear();
    }
}
