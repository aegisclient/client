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
import java.io.File;

public class LauncherFrame extends JFrame {

    // color palette matching Aegis branding
    private static final Color BG_DARK = new Color(10, 10, 15);
    private static final Color BG_PANEL = new Color(18, 18, 26);
    private static final Color BG_CARD = new Color(26, 26, 38);
    private static final Color BG_HOVER = new Color(34, 34, 50);
    private static final Color GOLD = new Color(230, 168, 23);
    private static final Color GOLD_LIGHT = new Color(240, 200, 80);
    private static final Color GOLD_DIM = new Color(196, 138, 0);
    private static final Color BLUE = new Color(0, 180, 255);
    private static final Color TEXT = new Color(224, 224, 232);
    private static final Color TEXT_DIM = new Color(136, 136, 160);
    private static final Color BORDER = new Color(30, 30, 46);
    private static final Color GREEN = new Color(46, 204, 113);
    private static final Color RED = new Color(231, 76, 60);

    private JTextArea consoleArea;
    private JLabel statusFabric, statusFabricApi, statusAegis;
    private JButton launchButton, installButton;
    private GameLauncher gameLauncher;

    public LauncherFrame() {
        setTitle("Aegis Launcher v" + AegisLauncher.VERSION);
        setSize(900, 620);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        gameLauncher = new GameLauncher(this::log);

        initUI();
        refreshStatus();
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
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // branding header
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(BG_PANEL);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(30, 20, 20, 20));
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        // separator
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BG_PANEL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);

        // status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BG_PANEL);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusTitle = new JLabel("STATUS");
        statusTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusTitle.setForeground(TEXT_DIM);
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.add(statusTitle);
        statusPanel.add(Box.createVerticalStrut(12));

        statusFabric = createStatusLabel("Fabric Loader");
        statusFabricApi = createStatusLabel("Fabric API");
        statusAegis = createStatusLabel("Aegis Client");

        statusPanel.add(createStatusRow(statusFabric, "Fabric Loader"));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusRow(statusFabricApi, "Fabric API"));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusRow(statusAegis, "Aegis Client"));

        sidebar.add(statusPanel);

        // separator
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER);
        sep2.setBackground(BG_PANEL);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep2);

        // modules info
        JPanel modulesPanel = new JPanel();
        modulesPanel.setBackground(BG_PANEL);
        modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
        modulesPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        modulesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel modTitle = new JLabel("MODULES");
        modTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        modTitle.setForeground(TEXT_DIM);
        modTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        modulesPanel.add(modTitle);
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

        // top action bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actionBar.setBackground(BG_DARK);
        actionBar.setBorder(new EmptyBorder(8, 8, 0, 8));

        launchButton = createStyledButton("LAUNCH MINECRAFT", GOLD, BG_DARK);
        launchButton.setPreferredSize(new Dimension(200, 42));
        launchButton.addActionListener(e -> onLaunch());

        installButton = createStyledButton("INSTALL MOD", BG_CARD, TEXT);
        installButton.setPreferredSize(new Dimension(150, 42));
        installButton.addActionListener(e -> onInstall());

        JButton refreshBtn = createStyledButton("REFRESH", BG_CARD, TEXT);
        refreshBtn.setPreferredSize(new Dimension(110, 42));
        refreshBtn.addActionListener(e -> refreshStatus());

        JButton folderBtn = createStyledButton("MODS FOLDER", BG_CARD, TEXT);
        folderBtn.setPreferredSize(new Dimension(140, 42));
        folderBtn.addActionListener(e -> openModsFolder());

        actionBar.add(launchButton);
        actionBar.add(installButton);
        actionBar.add(refreshBtn);
        actionBar.add(folderBtn);

        main.add(actionBar, BorderLayout.NORTH);

        // console output
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

        // welcome message
        log("===========================================");
        log("  AEGIS LAUNCHER v" + AegisLauncher.VERSION);
        log("  Minecraft " + AegisLauncher.MC_VERSION + " | Fabric");
        log("===========================================");
        log("");
        log("Welcome to Aegis.");
        log("Press LAUNCH to start Minecraft with Aegis.");
        log("Press INSTALL MOD to install the Aegis JAR.");
        log("");

        return main;
    }

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
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JLabel createStatusLabel(String name) {
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
            log("[!] Install Fabric Loader for MC 1.20.4 from https://fabricmc.net/use/");
        }
        if (!fabricApi) {
            log("[!] Download Fabric API and place it in your mods folder");
        }
        log("");
    }

    private void updateIndicator(JLabel indicator, boolean installed) {
        indicator.setForeground(installed ? GREEN : RED);
        indicator.setToolTipText(installed ? "Installed" : "Not found");
    }

    private void onLaunch() {
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

        // start in the project build output if it exists
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
            log("[Folder] Could not open folder: " + e.getMessage());
            log("[Folder] Path: " + modsDir.getAbsolutePath());
        }
        log("");
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
