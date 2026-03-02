package dev.aegis.client.module;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.combat.*;
import dev.aegis.client.module.movement.*;
import dev.aegis.client.module.render.*;
import dev.aegis.client.module.player.*;
import dev.aegis.client.module.world.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // combat
        register(new KillAura());
        register(new Reach());
        register(new Criticals());
        register(new AutoTotem());
        register(new AutoArmor());

        // movement
        register(new Flight());
        register(new Speed());
        register(new NoFall());
        register(new Sprint());
        register(new Step());
        register(new Jesus());
        register(new BoatFly());

        // render
        register(new ESP());
        register(new Fullbright());
        register(new Xray());
        register(new Tracers());
        register(new Nametags());
        register(new NoWeather());

        // player
        register(new AutoMine());
        register(new FastPlace());
        register(new AutoEat());
        register(new ChestStealer());
        register(new Scaffold());
        register(new AutoFish());

        // world
        register(new Nuker());
        register(new Timer());
        register(new AntiHunger());

        Aegis.LOGGER.info("Registered {} modules", modules.size());
    }

    private void register(Module mod) {
        modules.add(mod);
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // handle keybinds
        long window = mc.getWindow().getHandle();
        for (Module mod : modules) {
            if (mod.getKeyBind() != GLFW.GLFW_KEY_UNKNOWN) {
                if (GLFW.glfwGetKey(window, mod.getKeyBind()) == GLFW.GLFW_PRESS) {
                    // debounce - only toggle on first press
                    if (!keyStates.contains(mod.getKeyBind())) {
                        keyStates.add(mod.getKeyBind());
                        mod.toggle();
                    }
                } else {
                    keyStates.remove(Integer.valueOf(mod.getKeyBind()));
                }
            }
        }

        // tick enabled modules
        for (Module mod : modules) {
            if (mod.isEnabled()) {
                try {
                    mod.onTick();
                } catch (Exception e) {
                    Aegis.LOGGER.error("Error ticking module {}: {}", mod.getName(), e.getMessage());
                }
            }
        }
    }

    private final List<Integer> keyStates = new ArrayList<>();

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }
}
