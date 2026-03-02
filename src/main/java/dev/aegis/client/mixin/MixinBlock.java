package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import dev.aegis.client.module.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void onShouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        Module xray = Aegis.getInstance() != null ? Aegis.getInstance().getModuleManager().getModule("Xray") : null;
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(Xray.isXrayBlock(state.getBlock()));
        }
    }
}
