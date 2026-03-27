package dev.aegis.client.module.misc;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NameProtect extends Module {

    private String fakeName = "AegisUser";

    public NameProtect() {
        super("NameProtect", "Hides your real username in chat and nametags", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // name replacement handled via mixin on text rendering
    }

    public String getFakeName() { return fakeName; }
    public void setFakeName(String name) { this.fakeName = name; }

    public String getRealName() {
        if (mc.player != null) {
            return mc.player.getName().getString();
        }
        return "";
    }
}
