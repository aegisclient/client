package dev.aegis.client.module;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public abstract class Module {

    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private int keyBind;
    private boolean enabled;

    public Module(String name, String description, Category category, int keyBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = keyBind;
        this.enabled = false;
    }

    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        enabled = true;
        Aegis.getInstance().getEventBus().subscribe(this);
        onEnable();
        ChatHelper.info(name + " \u00a7aenabled");
    }

    public void disable() {
        enabled = false;
        Aegis.getInstance().getEventBus().unsubscribe(this);
        onDisable();
        ChatHelper.info(name + " \u00a7cdisabled");
    }

    // override these in modules
    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) { this.keyBind = keyBind; }
    public boolean isEnabled() { return enabled; }
}
