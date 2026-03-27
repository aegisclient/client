package dev.aegis.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final File configDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "aegis");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public void saveConfig(String name) {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        for (Module mod : Aegis.getInstance().getModuleManager().getModules()) {
            JsonObject modObj = new JsonObject();
            modObj.addProperty("enabled", mod.isEnabled());
            modObj.addProperty("keybind", mod.getKeyBind());
            modules.add(mod.getName(), modObj);
        }

        root.add("modules", modules);

        // save friends
        JsonObject friends = new JsonObject();
        var friendsList = Aegis.getInstance().getFriendManager().getFriends();
        friends.addProperty("list", String.join(",", friendsList));
        root.add("friends", friends);

        // save prefix
        root.addProperty("prefix", Aegis.getInstance().getCommandManager().getPrefix());

        File file = new File(configDir, name + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            Aegis.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public boolean loadConfig(String name) {
        File file = new File(configDir, name + ".json");
        if (!file.exists()) return false;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module mod : Aegis.getInstance().getModuleManager().getModules()) {
                    if (modules.has(mod.getName())) {
                        JsonObject modObj = modules.getAsJsonObject(mod.getName());
                        boolean enabled = modObj.has("enabled") && modObj.get("enabled").getAsBoolean();
                        int keybind = modObj.has("keybind") ? modObj.get("keybind").getAsInt() : mod.getKeyBind();

                        if (enabled && !mod.isEnabled()) mod.enable();
                        if (!enabled && mod.isEnabled()) mod.disable();
                        mod.setKeyBind(keybind);
                    }
                }
            }

            if (root.has("friends")) {
                String friendStr = root.getAsJsonObject("friends").get("list").getAsString();
                if (!friendStr.isEmpty()) {
                    for (String f : friendStr.split(",")) {
                        Aegis.getInstance().getFriendManager().addFriend(f.trim());
                    }
                }
            }

            if (root.has("prefix")) {
                Aegis.getInstance().getCommandManager().setPrefix(root.get("prefix").getAsString());
            }

            return true;
        } catch (Exception e) {
            Aegis.LOGGER.error("Failed to load config: {}", e.getMessage());
            return false;
        }
    }

    public List<String> listConfigs() {
        List<String> configs = new ArrayList<>();
        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                configs.add(f.getName().replace(".json", ""));
            }
        }
        return configs;
    }

    public void autoSave() {
        saveConfig("auto");
    }

    public void autoLoad() {
        loadConfig("auto");
    }
}
