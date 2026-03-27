package dev.aegis.launcher.util;

import java.io.File;

public class MinecraftPaths {

    public static File getMinecraftDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return new File(appdata != null ? appdata : home, ".minecraft");
        } else if (os.contains("mac")) {
            return new File(home, "Library/Application Support/minecraft");
        } else {
            return new File(home, ".minecraft");
        }
    }

    public static File getModsDir() {
        return new File(getMinecraftDir(), "mods");
    }

    public static File getVersionsDir() {
        return new File(getMinecraftDir(), "versions");
    }

    public static boolean isFabricInstalled() {
        File versionsDir = getVersionsDir();
        if (!versionsDir.exists()) return false;

        File[] versions = versionsDir.listFiles();
        if (versions == null) return false;

        for (File version : versions) {
            if (version.getName().contains("fabric") && version.getName().contains("1.20.4")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAegisInstalled() {
        File modsDir = getModsDir();
        if (!modsDir.exists()) return false;

        File[] mods = modsDir.listFiles();
        if (mods == null) return false;

        for (File mod : mods) {
            if (mod.getName().toLowerCase().contains("aegis")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFabricApiInstalled() {
        File modsDir = getModsDir();
        if (!modsDir.exists()) return false;

        File[] mods = modsDir.listFiles();
        if (mods == null) return false;

        for (File mod : mods) {
            String name = mod.getName().toLowerCase();
            if (name.contains("fabric-api") || name.contains("fabric_api")) {
                return true;
            }
        }
        return false;
    }

    public static String getFabricVersionId() {
        File versionsDir = getVersionsDir();
        if (!versionsDir.exists()) return null;

        File[] versions = versionsDir.listFiles();
        if (versions == null) return null;

        for (File version : versions) {
            if (version.getName().contains("fabric") && version.getName().contains("1.20.4")) {
                return version.getName();
            }
        }
        return null;
    }
}
