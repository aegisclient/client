package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class PacketMine extends Module {

    private BlockPos miningPos = null;
    private Direction miningFace = null;
    private int ticksMining = 0;

    public PacketMine() {
        super("PacketMine", "Mine blocks by sending packets - allows mining while doing other things", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // detect when player starts mining a block
        if (mc.options.attackKey.isPressed()) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
                BlockPos newPos = blockHit.getBlockPos();

                if (miningPos == null || !miningPos.equals(newPos)) {
                    miningPos = newPos;
                    miningFace = blockHit.getSide();
                    ticksMining = 0;

                    // send start mining packet
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                            miningPos, miningFace
                    ));
                }
            }
        }

        // continue mining even while doing other things
        if (miningPos != null) {
            ticksMining++;

            // send stop mining packet after enough time
            // the server will break the block based on tool speed
            if (ticksMining > 40 || mc.world.getBlockState(miningPos).isAir()) {
                if (!mc.world.getBlockState(miningPos).isAir()) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                            miningPos, miningFace
                    ));
                }
                miningPos = null;
                miningFace = null;
                ticksMining = 0;
            }
        }
    }

    @Override
    protected void onDisable() {
        miningPos = null;
        miningFace = null;
        ticksMining = 0;
    }
}
