package dev.aegis.client.module.premium;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AegisCape extends Module {

    private static final String CAPE_TEXTURE_ID = "aegis_premium_cape";
    private Identifier capeTextureId = null;
    private boolean textureRegistered = false;

    public AegisCape() {
        super("AegisCape", "Renders a custom Aegis shield cape on the player", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        if (!textureRegistered) {
            registerCapeTexture();
        }
    }

    @Override
    protected void onDisable() {
        // cape rendering is handled by the mixin checking isEnabled()
    }

    private void registerCapeTexture() {
        try {
            NativeImage image = generateShieldCape();
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);

            capeTextureId = mc.getTextureManager().registerDynamicTexture(CAPE_TEXTURE_ID, texture);
            textureRegistered = true;
        } catch (Exception e) {
            // fail silently - cape just won't render
        }
    }

    /**
     * Generates a 64x32 cape texture with the Aegis shield logo.
     * Cape UV layout: the visible cape area is at (1,1) to (11,17) in the 64x32 texture.
     */
    private NativeImage generateShieldCape() {
        NativeImage image = new NativeImage(64, 32, true);

        // fill entire image transparent
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                image.setColor(x, y, 0x00000000);
            }
        }

        // cape colors
        int bgColor = packColor(255, 20, 20, 25);        // dark background
        int shieldOutline = packColor(255, 230, 165, 30); // gold outline
        int shieldFill = packColor(255, 35, 35, 45);      // dark blue-grey fill
        int shieldInner = packColor(255, 200, 140, 25);   // inner gold accent
        int letterColor = packColor(255, 255, 200, 50);   // bright gold for "A"

        // fill cape background (cape area is roughly 10x16 at offset 1,1)
        for (int x = 1; x <= 10; x++) {
            for (int y = 1; y <= 16; y++) {
                image.setColor(x, y, bgColor);
            }
        }

        // draw shield outline (pointed bottom shield shape)
        // top edge
        for (int x = 3; x <= 8; x++) {
            image.setColor(x, 2, shieldOutline);
        }
        // left edge
        for (int y = 2; y <= 10; y++) {
            image.setColor(2, y, shieldOutline);
        }
        // right edge
        for (int y = 2; y <= 10; y++) {
            image.setColor(9, y, shieldOutline);
        }
        // bottom-left diagonal
        image.setColor(3, 11, shieldOutline);
        image.setColor(4, 12, shieldOutline);
        image.setColor(5, 13, shieldOutline);
        // bottom-right diagonal
        image.setColor(8, 11, shieldOutline);
        image.setColor(7, 12, shieldOutline);
        image.setColor(6, 13, shieldOutline);
        // bottom point
        image.setColor(5, 14, shieldOutline);
        image.setColor(6, 14, shieldOutline);

        // fill shield interior
        for (int x = 3; x <= 8; x++) {
            for (int y = 3; y <= 10; y++) {
                image.setColor(x, y, shieldFill);
            }
        }
        // fill lower triangle of shield
        for (int x = 4; x <= 7; x++) image.setColor(x, 11, shieldFill);
        for (int x = 5; x <= 6; x++) image.setColor(x, 12, shieldFill);

        // draw inner accent line (horizontal bar across shield)
        for (int x = 3; x <= 8; x++) {
            image.setColor(x, 6, shieldInner);
        }

        // draw "A" letter for Aegis (3 pixels wide, centered)
        // top of A
        image.setColor(5, 4, letterColor);
        image.setColor(6, 4, letterColor);
        // sides of A
        image.setColor(4, 5, letterColor);
        image.setColor(7, 5, letterColor);
        image.setColor(4, 6, letterColor);
        image.setColor(7, 6, letterColor);
        // crossbar of A (overlaps with accent line)
        image.setColor(5, 7, letterColor);
        image.setColor(6, 7, letterColor);
        // legs of A
        image.setColor(4, 7, letterColor);
        image.setColor(7, 7, letterColor);
        image.setColor(4, 8, letterColor);
        image.setColor(7, 8, letterColor);
        image.setColor(4, 9, letterColor);
        image.setColor(7, 9, letterColor);

        return image;
    }

    /**
     * Pack ARGB color in the format NativeImage expects (ABGR internally).
     */
    private int packColor(int a, int r, int g, int b) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public Identifier getCapeTextureId() {
        return capeTextureId;
    }

    public boolean isTextureReady() {
        return textureRegistered && capeTextureId != null;
    }
}
