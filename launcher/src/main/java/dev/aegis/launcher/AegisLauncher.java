package dev.aegis.launcher;

import dev.aegis.launcher.ui.LauncherFrame;

import javax.swing.*;

public class AegisLauncher {

    public static final String VERSION = "2.0.0";
    public static final String MC_VERSION = "1.20.4";
    public static final String CLIENT_NAME = "Aegis";

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Aegis Launcher");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LauncherFrame frame = new LauncherFrame();
            frame.setVisible(true);
        });
    }
}
