package dev.aegis.launcher.ui;

import dev.aegis.launcher.AegisLauncher;
import dev.aegis.launcher.util.GameLauncher;
import dev.aegis.launcher.util.MinecraftPaths;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;

public class LauncherFrame extends JFrame {

    private static final Color BG_DARK = new Color(10, 10, 15);
    private static final Color BG_PANEL = new Color(18, 18, 26);
    private static final Color BG_CARD = new Color(26, 26, 38);
    private static final Color GOLD = new Color(230, 168, 23);
    private static final Color GOLD_DIM = new Color(196, 138, 0);
    private static final Color BLUE = new Color(0, 153, 221);
    private static final Color TEXT = new Color(224, 224, 232);
    private static final Color TEXT_DIM = new Color(136, 136, 160);
    private static final Color BORDER = new Color(30, 30, 46);
    private static final Color GREEN = new Color(46, 204, 113);
    private static final Color RED = new Color(231, 76, 60);

    private JTextArea consoleArea;
    private JLabel statusFabric, statusFabricApi, statusAegis;
    private JButton launchButton;
    private GameLauncher gameLauncher;

    public LauncherFrame() {
        setTitle("Aegis Launcher v" + AegisLauncher.VERSION);
        setSize(960, 640);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        gameLauncher = new GameLauncher(this::log);

        initUI();
        refreshStatus();
        autoInstallAegis();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // branding
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(BG_PANEL);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel title = new JLabel("AEGIS");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(title);

        JLabel subtitle = new JLabel("Utility Client");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_DIM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(subtitle);

        JLabel version = new JLabel("v" + AegisLauncher.VERSION + " \u2022 MC " + AegisLauncher.MC_VERSION);
        version.setFont(new Font("Monospaced", Font.PLAIN, 11));
        version.setForeground(TEXT_DIM);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(version);

        sidebar.add(brandPanel);
        sidebar.add(createSeparator());

        // status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BG_PANEL);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        statusPanel.add(createSectionLabel("STATUS"));
        statusPanel.add(Box.createVerticalStrut(12));

        statusFabric = createStatusDot();
        statusFabricApi = createStatusDot();
        statusAegis = createStatusDot();

        statusPanel.add(createStatusRow(statusFabric, "Fabric Loader"));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusRow(statusFabricApi, "Fabric API"));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusRow(statusAegis, "Aegis Client"));

        sidebar.add(statusPanel);
        sidebar.add(createSeparator());

        // modules
        JPanel modulesPanel = new JPanel();
        modulesPanel.setBackground(BG_PANEL);
        modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
        modulesPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        modulesPanel.add(createSectionLabel("MODULES"));
        modulesPanel.add(Box.createVerticalStrut(10));

        String[][] cats = {
                {"Combat", "6", "#e74c3c"},
                {"Movement", "7", "#3498db"},
                {"Render", "6", "#2ecc71"},
                {"Player", "6", "#f39c12"},
                {"World", "3", "#9b59b6"}
        };

        for (String[] cat : cats) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row.setBackground(BG_PANEL);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

            JLabel dot = new JLabel("\u25CF ");
            dot.setForeground(Color.decode(cat[2]));
            dot.setFont(new Font("SansSerif", Font.PLAIN, 10));

            JLabel name = new JLabel(cat[0]);
            name.setForeground(TEXT);
            name.setFont(new Font("SansSerif", Font.PLAIN, 12));

            JLabel count = new JLabel("  " + cat[1]);
            count.setForeground(TEXT_DIM);
            count.setFont(new Font("Monospaced", Font.PLAIN, 11));

            row.add(dot);
            row.add(name);
            row.add(count);
            modulesPanel.add(row);
            modulesPanel.add(Box.createVerticalStrut(4));
        }

        JLabel total = new JLabel("28 modules total");
        total.setFont(new Font("SansSerif", Font.ITALIC, 11));
        total.setForeground(GOLD_DIM);
        total.setAlignmentX(Component.LEFT_ALIGNMENT);
        modulesPanel.add(Box.createVerticalStrut(8));
        modulesPanel.add(total);

        sidebar.add(modulesPanel);
        sidebar.add(Box.createVerticalGlue());

        // footer
        JPanel footer = new JPanel();
        footer.setBackground(BG_PANEL);
        footer.setBorder(new EmptyBorder(10, 20, 15, 20));
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel footerLabel = new JLabel("Single-player testing only");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        footerLabel.setForeground(TEXT_DIM);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(footerLabel);
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG_DARK);

        // action bar with two rows
        JPanel actionBar = new JPanel();
        actionBar.setBackground(BG_DARK);
        actionBar.setLayout(new BoxLayout(actionBar, BoxLayout.Y_AXIS));
        actionBar.setBorder(new EmptyBorder(8, 8, 0, 8));

        // row 1: main actions
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row1.setBackground(BG_DARK);

        launchButton = createStyledButton("LAUNCH MINECRAFT", GOLD, BG_DARK);
        launchButton.setPreferredSize(new Dimension(200, 42));
        launchButton.addActionListener(e -> onLaunch());

        JButton installBtn = createStyledButton("INSTALL MOD", BG_CARD, TEXT);
        installBtn.setPreferredSize(new Dimension(140, 42));
        installBtn.addActionListener(e -> onInstall());

        JButton refreshBtn = createStyledButton("REFRESH", BG_CARD, TEXT);
        refreshBtn.setPreferredSize(new Dimension(100, 42));
        refreshBtn.addActionListener(e -> refreshStatus());

        JButton folderBtn = createStyledButton("MODS FOLDER", BG_CARD, TEXT);
        folderBtn.setPreferredSize(new Dimension(130, 42));
        folderBtn.addActionListener(e -> openModsFolder());

        row1.add(launchButton);
        row1.add(installBtn);
        row1.add(refreshBtn);
        row1.add(folderBtn);

        // row 2: setup helpers
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setBackground(BG_DARK);

        JButton fabricBtn = createStyledButton("GET FABRIC LOADER", BLUE, BG_DARK);
        fabricBtn.setPreferredSize(new Dimension(180, 34));
        fabricBtn.addActionListener(e -> downloadFabricInstaller());

        JButton fabricApiBtn = createStyledButton("GET FABRIC API", BLUE, BG_DARK);
        fabricApiBtn.setPreferredSize(new Dimension(160, 34));
        fabricApiBtn.addActionListener(e -> downloadFabricApi());

        row2.add(fabricBtn);
        row2.add(fabricApiBtn);

        actionBar.add(row1);
        actionBar.add(row2);

        main.add(actionBar, BorderLayout.NORTH);

        // console
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(BG_DARK);
        consolePanel.setBorder(new EmptyBorder(8, 20, 20, 20));

        JLabel consoleTitle = new JLabel("  CONSOLE");
        consoleTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        consoleTitle.setForeground(TEXT_DIM);
        consoleTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        consolePanel.add(consoleTitle, BorderLayout.NORTH);

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(8, 8, 12));
        consoleArea.setForeground(TEXT);
        consoleArea.setCaretColor(GOLD);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getVerticalScrollBar().setBackground(BG_DARK);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        main.add(consolePanel, BorderLayout.CENTER);

        log("===========================================");
        log("  AEGIS LAUNCHER v" + AegisLauncher.VERSION);
        log("  Minecraft " + AegisLauncher.MC_VERSION + " | Fabric");
        log("===========================================");
        log("");
        log("Keybinds:");
        log("  Right Shift  - Open ClickGUI");
        log("  R - KillAura  |  F - Flight  |  V - Speed");
        log("  B - Fullbright |  X - Xray   |  G - Scaffold");
        log("  J - Jesus      |  N - Nuker");
        log("");

        return main;
    }

    private void autoInstallAegis() {
        // look for the built JAR relative to the launcher
        File jarDir = new File(System.getProperty("user.dir"));
        File[] searchPaths = {
                new File(jarDir, "../build/libs/aegis-client-1.0.0.jar"),
                new File(jarDir, "build/libs/aegis-client-1.0.0.jar"),
                new File(jarDir.getParentFile(), "build/libs/aegis-client-1.0.0.jar"),
        };

        File foundJar = null;
        for (File path : searchPaths) {
            if (path.exists()) {
                foundJar = path;
                break;
            }
        }

        if (foundJar != null && !MinecraftPaths.isAegisInstalled()) {
            File jar = foundJar;
            log("[Auto] Found Aegis JAR: " + jar.getName());
            new Thread(() -> {
                try {
                    gameLauncher.installAegisMod(jar);
                    SwingUtilities.invokeLater(() -> {
                        log("[Auto] Aegis installed to mods folder!");
                        log("");
                        refreshStatus();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> log("[Auto] Could not auto-install: " + e.getMessage()));
                }
            }).start();
        } else if (foundJar != null) {
            log("[Auto] Aegis already installed in mods folder.");
            log("");
        }
    }

    private void downloadFabricInstaller() {
        log("[Fabric] Downloading Fabric Installer...");
        new Thread(() -> {
            try {
                String installerUrl = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.1/fabric-installer-1.0.1.jar";
                File tmpDir = new File(System.getProperty("java.io.tmpdir"));
                File installerFile = new File(tmpDir, "fabric-installer.jar");

                try (InputStream in = new URL(installerUrl).openStream()) {
                    Files.copy(in, installerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                SwingUtilities.invokeLater(() -> {
                    log("[Fabric] Downloaded! Launching installer...");
                    log("[Fabric] Select MC version 1.20.4 and click Install.");
                });

                // launch the fabric installer
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", installerFile.getAbsolutePath());
                pb.start();

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("[Fabric] Download failed: " + e.getMessage());
                    log("[Fabric] Download manually from: https://fabricmc.net/use/");
                    try {
                        Desktop.getDesktop().browse(new URI("https://fabricmc.net/use/"));
                    } catch (Exception ignored) {}
                });
            }
            SwingUtilities.invokeLater(() -> log(""));
        }).start();
    }

    private void downloadFabricApi() {
        log("[Fabric API] Downloading Fabric API for MC 1.20.4...");
        new Thread(() -> {
            try {
                // Modrinth API to get the download URL
                String apiUrl = "https://cdn.modrinth.com/data/P7dR8mSH/versions/tFw0iWAk/fabric-api-0.97.3%2B1.20.4.jar";
                File modsDir = MinecraftPaths.getModsDir();
                if (!modsDir.exists()) modsDir.mkdirs();

                File dest = new File(modsDir, "fabric-api-0.97.3+1.20.4.jar");

                try (InputStream in = new URL(apiUrl).openStream()) {
                    Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                SwingUtilities.invokeLater(() -> {
                    log("[Fabric API] Installed to mods folder!");
                    refreshStatus();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("[Fabric API] Download failed: " + e.getMessage());
                    log("[Fabric API] Download manually from Modrinth");
                    try {
                        Desktop.getDesktop().browse(new URI("https://modrinth.com/mod/fabric-api/versions?g=1.20.4"));
                    } catch (Exception ignored) {}
                });
            }
            SwingUtilities.invokeLater(() -> log(""));
        }).start();
    }

    private void refreshStatus() {
        boolean fabric = MinecraftPaths.isFabricInstalled();
        boolean fabricApi = MinecraftPaths.isFabricApiInstalled();
        boolean aegis = MinecraftPaths.isAegisInstalled();

        updateIndicator(statusFabric, fabric);
        updateIndicator(statusFabricApi, fabricApi);
        updateIndicator(statusAegis, aegis);

        log("[Status] Fabric Loader: " + (fabric ? "INSTALLED" : "NOT FOUND"));
        log("[Status] Fabric API: " + (fabricApi ? "INSTALLED" : "NOT FOUND"));
        log("[Status] Aegis Client: " + (aegis ? "INSTALLED" : "NOT FOUND"));

        if (!fabric) {
            log("[!] Click GET FABRIC LOADER above to install it");
        }
        if (!fabricApi) {
            log("[!] Click GET FABRIC API above to download it");
        }
        if (fabric && fabricApi && aegis) {
            log("[OK] Everything installed! Click LAUNCH MINECRAFT to play.");
        }
        log("");
    }

    private void onLaunch() {
        if (!MinecraftPaths.isFabricInstalled()) {
            log("[Launch] Fabric Loader not installed!");
            log("[Launch] Click GET FABRIC LOADER to install it first.");
            log("");
            return;
        }

        log("[Launch] Starting Minecraft...");
        launchButton.setEnabled(false);

        new Thread(() -> {
            boolean success = gameLauncher.launchMinecraft();
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    log("[Launch] Done! Use Right Shift in-game to open ClickGUI.");
                } else {
                    log("[Launch] Failed to launch. Check the errors above.");
                }
                log("");
                launchButton.setEnabled(true);
            });
        }).start();
    }

    private void onInstall() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Aegis JAR");
        chooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));

        File buildLibs = new File(System.getProperty("user.dir"), "../build/libs");
        if (buildLibs.exists()) {
            chooser.setCurrentDirectory(buildLibs);
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            log("[Install] Installing " + selected.getName() + "...");

            new Thread(() -> {
                try {
                    gameLauncher.installAegisMod(selected);
                    SwingUtilities.invokeLater(() -> {
                        log("[Install] Success! Aegis is now in your mods folder.");
                        log("");
                        refreshStatus();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        log("[Install] ERROR: " + e.getMessage());
                        log("");
                    });
                }
            }).start();
        }
    }

    private void openModsFolder() {
        File modsDir = MinecraftPaths.getModsDir();
        if (!modsDir.exists()) {
            modsDir.mkdirs();
            log("[Folder] Created mods directory");
        }
        try {
            Desktop.getDesktop().open(modsDir);
            log("[Folder] Opened: " + modsDir.getAbsolutePath());
        } catch (Exception e) {
            log("[Folder] Could not open: " + e.getMessage());
            log("[Folder] Path: " + modsDir.getAbsolutePath());
        }
        log("");
    }

    // helper methods
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BG_PANEL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(TEXT_DIM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createStatusDot() {
        JLabel label = new JLabel("\u25CF");
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        return label;
    }

    private JPanel createStatusRow(JLabel indicator, String name) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(BG_PANEL);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel label = new JLabel("  " + name);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        row.add(indicator);
        row.add(label);
        return row;
    }

    private void updateIndicator(JLabel indicator, boolean installed) {
        indicator.setForeground(installed ? GREEN : RED);
        indicator.setToolTipText(installed ? "Installed" : "Not found");
    }

    private void log(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            consoleArea.append(message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> {
                consoleArea.append(message + "\n");
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            });
        }
    }
}
