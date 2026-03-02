package dev.aegis.client;

import dev.aegis.client.event.EventBus;
import dev.aegis.client.gui.ClickGui;
import dev.aegis.client.gui.HudOverlay;
import dev.aegis.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aegis implements ClientModInitializer {

    public static final String NAME = "Aegis";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private static Aegis INSTANCE;
    private ModuleManager moduleManager;
    private EventBus eventBus;
    private ClickGui clickGui;
    private HudOverlay hudOverlay;

    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Loading {} v{}", NAME, VERSION);

        eventBus = new EventBus();
        moduleManager = new ModuleManager();
        clickGui = new ClickGui();
        hudOverlay = new HudOverlay();

        moduleManager.init();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Aegis GUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Aegis"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // handle gui toggle
            if (openGuiKey.wasPressed()) {
                MinecraftClient.getInstance().setScreen(clickGui);
            }

            moduleManager.onTick();
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            hudOverlay.render(drawContext);
        });

        LOGGER.info("{} loaded successfully! {} modules registered.", NAME, moduleManager.getModules().size());
    }

    public static Aegis getInstance() {
        return INSTANCE;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }
}
