package dev.aegis.client.module.premium;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import org.lwjgl.glfw.GLFW;

public class PremiumInfo extends Module {

    public PremiumInfo() {
        super("PremiumInfo", "Shows premium status, contact info, and custom module request details", Category.PREMIUM, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    protected void onEnable() {
        showPremiumInfo();
        // auto-disable after showing info
        disable();
    }

    private void showPremiumInfo() {
        ChatHelper.info("\u00a76\u00a7l==============================");
        ChatHelper.info("\u00a7e\u00a7l  Aegis Premium \u00a7av" + Aegis.VERSION);
        ChatHelper.info("\u00a76\u00a7l==============================");
        ChatHelper.info("");
        ChatHelper.info("\u00a77Status: \u00a7aPREMIUM ACTIVE");
        ChatHelper.info("");
        ChatHelper.info("\u00a7ePremium Features:");
        ChatHelper.info("\u00a77  - Hypixel Watchdog bypasses");
        ChatHelper.info("\u00a77  - HypixelDisabler, Speed, Flight, Scaffold, KillAura");
        ChatHelper.info("\u00a77  - Multi-server bypasses (Vulcan, Matrix, Grim)");
        ChatHelper.info("\u00a77  - Custom Aegis cape");
        ChatHelper.info("");
        ChatHelper.info("\u00a7eCustom Module Requests:");
        ChatHelper.info("\u00a77  Discord: \u00a7baegis.dev");
        ChatHelper.info("\u00a77  Telegram: \u00a7b@aegisclient");
        ChatHelper.info("\u00a77  Email: \u00a7bpremium@aegis.dev");
        ChatHelper.info("");
        ChatHelper.info("\u00a76\u00a7l==============================");
    }

    @Override
    public void onTick() {
        // no tick behavior needed
    }
}
