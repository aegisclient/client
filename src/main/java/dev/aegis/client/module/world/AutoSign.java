package dev.aegis.client.module.world;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class AutoSign extends Module {

    private String[] signText = {"Aegis", "was here", "", ""};

    public AutoSign() {
        super("AutoSign", "Automatically fills sign text when placing signs", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        // sign editing is handled via screen events
        // when a sign edit screen opens, it auto-fills the text
    }

    public String[] getSignText() {
        return signText;
    }

    public void setSignText(String[] text) {
        this.signText = text;
    }
}
