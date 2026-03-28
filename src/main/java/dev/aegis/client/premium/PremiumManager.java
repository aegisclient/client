package dev.aegis.client.premium;

import dev.aegis.client.Aegis;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class PremiumManager {

    private static final String KEY_FILE = "aegis-premium.key";
    private static final String PREMIUM_MARKER = "/aegis-premium-marker";
    private boolean premium;

    public void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path keyFile = configDir.resolve(KEY_FILE);

        // Check if this JAR is a premium build (has the marker resource bundled inside)
        boolean bundledPremium = getClass().getResource(PREMIUM_MARKER) != null;
        if (bundledPremium && !Files.exists(keyFile)) {
            try {
                Files.createDirectories(configDir);
                Files.writeString(keyFile, "AEGIS-PREMIUM-ACTIVATED");
            } catch (Exception e) {
                Aegis.LOGGER.warn("Could not write premium key file: " + e.getMessage());
            }
        }

        premium = Files.exists(keyFile) && Files.isRegularFile(keyFile);

        if (premium) {
            Aegis.LOGGER.info("Premium key detected! Unlocking premium modules.");
        } else {
            Aegis.LOGGER.info("No premium key found. Premium modules disabled.");
        }
    }

    public boolean isPremium() {
        return premium;
    }
}
