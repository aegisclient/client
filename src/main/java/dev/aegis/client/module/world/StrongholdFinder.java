package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class StrongholdFinder extends Module {

    private final List<Vec3d> eyePositions = new ArrayList<>();
    private final List<Vec3d> eyeDirections = new ArrayList<>();

    public StrongholdFinder() {
        super("StrongholdFinder", "Triangulates stronghold position from two eye of ender throws", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // wait for user to throw eyes and record positions
    }

    public void recordThrow(Vec3d pos, Vec3d direction) {
        eyePositions.add(pos);
        eyeDirections.add(direction);

        if (eyePositions.size() >= 2) {
            Vec3d result = triangulate(
                    eyePositions.get(0), eyeDirections.get(0),
                    eyePositions.get(1), eyeDirections.get(1)
            );
            if (result != null) {
                ChatHelper.info(String.format("Stronghold at: %.0f, %.0f", result.x, result.z));
            } else {
                ChatHelper.info("Could not triangulate. Lines may be parallel.");
            }
            eyePositions.clear();
            eyeDirections.clear();
        } else {
            ChatHelper.info("Recorded throw 1/2. Throw another from a different location.");
        }
    }

    private Vec3d triangulate(Vec3d p1, Vec3d d1, Vec3d p2, Vec3d d2) {
        double dx = p2.x - p1.x;
        double dz = p2.z - p1.z;
        double cross = d1.x * d2.z - d1.z * d2.x;

        if (Math.abs(cross) < 0.001) return null;

        double t = (dx * d2.z - dz * d2.x) / cross;
        return new Vec3d(p1.x + t * d1.x, 0, p1.z + t * d1.z);
    }

    @Override
    protected void onDisable() {
        eyePositions.clear();
        eyeDirections.clear();
    }
}
