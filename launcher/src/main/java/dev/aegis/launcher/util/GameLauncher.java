package dev.aegis.launcher.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameLauncher {

    private Consumer<String> logger;

    public GameLauncher(Consumer<String> logger) {
        this.logger = logger;
    }

    public void installAegisMod(File aegisJar) throws IOException {
        File modsDir = MinecraftPaths.getModsDir();
        if (!modsDir.exists()) {
            modsDir.mkdirs();
            logger.accept("Created mods directory");
        }

        // remove old aegis versions
        File[] existing = modsDir.listFiles();
        if (existing != null) {
            for (File file : existing) {
                if (file.getName().toLowerCase().contains("aegis")) {
                    file.delete();
                    logger.accept("Removed old version: " + file.getName());
                }
            }
        }

        // copy new jar
        File dest = new File(modsDir, aegisJar.getName());
        Files.copy(aegisJar.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        logger.accept("Installed: " + aegisJar.getName());
    }

    public boolean launchMinecraft() {
        String fabricVersion = MinecraftPaths.getFabricVersionId();
        if (fabricVersion == null) {
            logger.accept("ERROR: Fabric not found for MC 1.20.4");
            logger.accept("Install Fabric Loader first from https://fabricmc.net/use/");
            return false;
        }

        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("mac")) {
                return launchOnMac();
            } else if (os.contains("win")) {
                return launchOnWindows();
            } else {
                return launchOnLinux();
            }
        } catch (Exception e) {
            logger.accept("ERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean launchOnMac() throws IOException {
        // try to launch via open command with minecraft
        File mcApp = new File("/Applications/Minecraft.app");
        if (!mcApp.exists()) {
            // check alternative location
            String home = System.getProperty("user.home");
            mcApp = new File(home + "/Applications/Minecraft.app");
        }

        if (mcApp.exists()) {
            logger.accept("Launching Minecraft...");
            ProcessBuilder pb = new ProcessBuilder("open", mcApp.getAbsolutePath());
            pb.start();
            logger.accept("Minecraft opened! Select the Fabric 1.20.4 profile and click Play.");
            return true;
        }

        logger.accept("ERROR: Minecraft.app not found");
        logger.accept("Make sure Minecraft is installed in /Applications/");
        return false;
    }

    private boolean launchOnWindows() throws IOException {
        // try minecraft launcher from standard locations
        String localAppData = System.getenv("LOCALAPPDATA");
        List<String> paths = new ArrayList<>();
        paths.add(localAppData + "\\Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local\\runtime");

        // try the new launcher
        File newLauncher = new File(localAppData + "\\Programs\\Minecraft Launcher\\MinecraftLauncher.exe");
        File oldLauncher = new File("C:\\Program Files (x86)\\Minecraft Launcher\\MinecraftLauncher.exe");

        File launcher = newLauncher.exists() ? newLauncher : oldLauncher.exists() ? oldLauncher : null;

        if (launcher != null) {
            logger.accept("Launching Minecraft...");
            ProcessBuilder pb = new ProcessBuilder(launcher.getAbsolutePath());
            pb.start();
            logger.accept("Minecraft opened! Select the Fabric 1.20.4 profile and click Play.");
            return true;
        }

        // fallback: try via protocol handler
        logger.accept("Launching via protocol handler...");
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "minecraft://"});
        logger.accept("Minecraft should be opening. Select the Fabric 1.20.4 profile.");
        return true;
    }

    private boolean launchOnLinux() throws IOException {
        // try common linux launcher locations
        String home = System.getProperty("user.home");
        File launcher = new File(home + "/.local/share/applications/minecraft-launcher");

        if (launcher.exists()) {
            ProcessBuilder pb = new ProcessBuilder(launcher.getAbsolutePath());
            pb.start();
            logger.accept("Minecraft opened! Select the Fabric 1.20.4 profile and click Play.");
            return true;
        }

        // try flatpak
        try {
            ProcessBuilder pb = new ProcessBuilder("flatpak", "run", "com.mojang.Minecraft");
            pb.start();
            logger.accept("Minecraft opened via Flatpak!");
            return true;
        } catch (Exception ignored) {}

        logger.accept("ERROR: Could not find Minecraft launcher");
        logger.accept("Launch Minecraft manually and select the Fabric 1.20.4 profile.");
        return false;
    }
}
