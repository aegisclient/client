package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AirPlace extends Module {

    public AirPlace() {
        super("AirPlace", "Place blocks in the air without needing an adjacent block", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.options.useKey.isPressed()) return;

        HitResult hit = mc.player.raycast(4.5, 1.0f, false);
        if (hit.getType() != HitResult.Type.MISS) return;

        // get the block pos the player is looking at in the air
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        Vec3d target = eyePos.add(lookVec.multiply(4.0));

        BlockPos pos = new BlockPos((int) Math.floor(target.x), (int) Math.floor(target.y), (int) Math.floor(target.z));

        if (!mc.world.getBlockState(pos).isAir()) return;

        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(pos),
                Direction.UP,
                pos,
                false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
    }
}
