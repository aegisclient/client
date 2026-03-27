package dev.aegis.client.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    // render hooks handled via Fabric API HudRenderCallback
}
