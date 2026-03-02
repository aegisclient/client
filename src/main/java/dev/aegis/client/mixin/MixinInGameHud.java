package dev.aegis.client.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    // HUD rendering is handled via HudRenderCallback for cleaner code
}
