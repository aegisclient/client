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
    private static final Color BG_INPUT = new Color(20, 20, 30);
    private static final Color GOLD = new Color(230, 168, 23);
    private static final Color GOLD_DIM = new Color(196, 138, 0);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color TEXT = new Color(224, 224, 232);
    private static final Color TEXT_DIM = new Color(136, 136, 160);
    private static final Color BORDER = new Color(30, 30, 46);
    private static final Color GREEN = new Color(46, 204, 113);
    private static final Color RED = new Color(231, 76, 60);

    private static final String API_URL = "http://185.197.250.205:3000";
    private static final String KEY_FILE_NAME = ".aegis-premium-key";

    private JTextArea consoleArea;
    private JLabel statusJava, statusMinecraft, statusFabric, statusFabricApi, statusAegis;
    private JLabel premiumBadge;
    private JButton launchButton, installButton;
    private JButton freeButton, premiumButton;
    private JTextField usernameField;
    private JSpinner memorySpinner;
    private GameLauncher gameLauncher;
    private boolean premiumEdition = false;
    private boolean premiumUnlocked = false;

    public LauncherFrame() {
        setTitle("Aegis Launcher");
        setSize(1000, 680);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        gameLauncher = new GameLauncher(this::log);

        // Check if premium was previously unlocked
        premiumUnlocked = loadPremiumKey();
        premiumEdition = premiumUnlocked;

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

    // ==================== SIDEBAR ====================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // branding
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(BG_PANEL);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JLabel title = new JLabel("AEGIS");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(BLUE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(title);

        JLabel version = new JLabel("v" + AegisLauncher.VERSION + " | MC " + AegisLauncher.MC_VERSION);
        version.setFont(new Font("Monospaced", Font.PLAIN, 12));
        version.setForeground(TEXT_DIM);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(version);

        premiumBadge = new JLabel("\u2605 PREMIUM");
        premiumBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        premiumBadge.setForeground(GOLD);
        premiumBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        premiumBadge.setVisible(premiumUnlocked);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(premiumBadge);

        sidebar.add(brandPanel);
        sidebar.add(createSeparator());

        // status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BG_PANEL);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        statusJava = createStatusDot();
        statusMinecraft = createStatusDot();
        statusFabric = createStatusDot();
        statusFabricApi = createStatusDot();
        statusAegis = createStatusDot();

        statusPanel.add(createStatusRow(statusJava, "Java", true));
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(createStatusRow(statusMinecraft, "Minecraft", true));
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(createStatusRow(statusFabric, "Fabric", false));
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(createStatusRow(statusFabricApi, "Fabric API", false));
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(createStatusRow(statusAegis, "Aegis", false));

        sidebar.add(statusPanel);
        sidebar.add(createSeparator());

        sidebar.add(Box.createVerticalGlue());

        // module count badge
        JPanel badgePanel = new JPanel();
        badgePanel.setBackground(BG_PANEL);
        badgePanel.setBorder(new EmptyBorder(10, 30, 20, 30));
        badgePanel.setLayout(new BoxLayout(badgePanel, BoxLayout.Y_AXIS));
        badgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel badgeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 30, 60), getWidth(), getHeight(), new Color(30, 20, 50));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(59, 130, 246, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        badgeCard.setLayout(new BoxLayout(badgeCard, BoxLayout.Y_AXIS));
        badgeCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        badgeCard.setOpaque(false);
        badgeCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countLabel = new JLabel("142");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        countLabel.setForeground(BLUE);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgeCard.add(countLabel);

        JLabel modulesLabel = new JLabel("MODULES");
        modulesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        modulesLabel.setForeground(TEXT_DIM);
        modulesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgeCard.add(modulesLabel);

        badgePanel.add(badgeCard);
        sidebar.add(badgePanel);

        return sidebar;
    }

    // ==================== MAIN PANEL ====================

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG_DARK);

        // top section with controls
        JPanel topPanel = new JPanel();
        topPanel.setBackground(BG_DARK);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(24, 28, 0, 28));

        // "Launch Minecraft" header
        JLabel header = new JLabel("Launch Minecraft");
        header.setFont(new Font("SansSerif", Font.BOLD, 26));
        header.setForeground(TEXT);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(header);

        // gradient separator line
        topPanel.add(Box.createVerticalStrut(12));
        JPanel gradLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, BLUE, getWidth(), 0, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        gradLine.setPreferredSize(new Dimension(0, 2));
        gradLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        gradLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(gradLine);

        // username + memory row
        topPanel.add(Box.createVerticalStrut(20));
        JPanel inputRow = new JPanel(new GridLayout(1, 2, 20, 0));
        inputRow.setBackground(BG_DARK);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        inputRow.add(createInputGroup("USERNAME", createUsernameField()));
        inputRow.add(createInputGroup("MEMORY (MB)", createMemorySpinner()));

        topPanel.add(inputRow);

        // edition selector
        topPanel.add(Box.createVerticalStrut(16));
        JPanel editionPanel = createEditionPanel();
        editionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(editionPanel);

        // action buttons
        topPanel.add(Box.createVerticalStrut(16));
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 16, 0));
        actionRow.setBackground(BG_DARK);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        installButton = createStyledButton("INSTALL", BG_CARD, TEXT);
        installButton.setPreferredSize(new Dimension(0, 48));
        installButton.addActionListener(e -> onInstall());

        launchButton = createGoldButton(premiumEdition ? "\u2605 LAUNCH PREMIUM" : "LAUNCH FREE");
        launchButton.setPreferredSize(new Dimension(0, 48));
        launchButton.addActionListener(e -> onLaunch());

        actionRow.add(installButton);
        actionRow.add(launchButton);
        topPanel.add(actionRow);

        main.add(topPanel, BorderLayout.NORTH);

        // console
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(BG_DARK);
        consolePanel.setBorder(new EmptyBorder(16, 28, 24, 28));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(8, 8, 12));
        consoleArea.setForeground(new Color(100, 160, 255));
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

        return main;
    }

    private JPanel createInputGroup(String label, JComponent field) {
        JPanel group = new JPanel();
        group.setBackground(BG_DARK);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(6));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(field);

        return group;
    }

    private JTextField createUsernameField() {
        usernameField = new JTextField("Player");
        usernameField.setBackground(BG_INPUT);
        usernameField.setForeground(TEXT);
        usernameField.setCaretColor(TEXT);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return usernameField;
    }

    private JSpinner createMemorySpinner() {
        memorySpinner = new JSpinner(new SpinnerNumberModel(4096, 1024, 16384, 512));
        memorySpinner.setBackground(BG_INPUT);
        memorySpinner.setForeground(TEXT);
        memorySpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        memorySpinner.setBorder(BorderFactory.createLineBorder(BORDER));

        JComponent editor = memorySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT);
            tf.setCaretColor(TEXT);
            tf.setBorder(new EmptyBorder(8, 12, 8, 12));
        }
        memorySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return memorySpinner;
    }

    private JPanel createEditionPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("EDITION");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 0, 0));
        btnRow.setBackground(BG_DARK);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        freeButton = createEditionButton("FREE", !premiumEdition);
        premiumButton = createEditionButton("\u2605 PREMIUM", premiumEdition);

        freeButton.addActionListener(e -> selectEdition(false));
        premiumButton.addActionListener(e -> selectEdition(true));

        btnRow.add(freeButton);
        btnRow.add(premiumButton);
        panel.add(btnRow);

        return panel;
    }

    private JButton createEditionButton(String text, boolean selected) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = text.contains("PREMIUM") ? premiumEdition : !premiumEdition;
                if (isActive) {
                    if (text.contains("PREMIUM")) {
                        GradientPaint gp = new GradientPaint(0, 0, GOLD, getWidth(), 0, new Color(255, 180, 0));
                        g2.setPaint(gp);
                    } else {
                        g2.setColor(BG_CARD);
                    }
                } else {
                    g2.setColor(BG_CARD.darker());
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(selected && text.contains("PREMIUM") ? BG_DARK : TEXT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectEdition(boolean premium) {
        if (premium && !premiumUnlocked) {
            String input = showPremiumKeyDialog();
            if (input == null) return;

            log("[Premium] Validating key with server...");
            // Validate against API on background thread
            String keyInput = input;
            new Thread(() -> {
                boolean valid = validateKeyWithApi(keyInput);
                SwingUtilities.invokeLater(() -> {
                    if (valid) {
                        premiumUnlocked = true;
                        savePremiumKey(keyInput);
                        premiumBadge.setVisible(true);
                        log("[Premium] Key accepted! Premium edition unlocked.");
                        log("");
                        applyEdition(true);
                    } else {
                        log("[Premium] Invalid or revoked key. Contact arhanh1234@gmail.com to purchase.");
                        log("");
                        JOptionPane.showMessageDialog(LauncherFrame.this,
                                "Invalid premium key.\n\nEmail arhanh1234@gmail.com to purchase a premium key.",
                                "Invalid Key", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
            return;
        }

        applyEdition(premium);
    }

    private void applyEdition(boolean premium) {
        premiumEdition = premium;
        freeButton.setForeground(premium ? TEXT_DIM : TEXT);
        premiumButton.setForeground(premium ? BG_DARK : TEXT_DIM);
        freeButton.repaint();
        premiumButton.repaint();
        launchButton.setText(premium ? "\u2605 LAUNCH PREMIUM" : "LAUNCH FREE");
        launchButton.repaint();
    }

    private boolean validateKeyWithApi(String key) {
        try {
            java.net.URL url = new java.net.URL(API_URL + "/api/validate-key");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String username = usernameField.getText().trim();
            String json = "{\"key\":\"" + key.replace("\"", "\\\"") + "\",\"username\":\"" + username.replace("\"", "\\\"") + "\"}";
            conn.getOutputStream().write(json.getBytes());

            int code = conn.getResponseCode();
            if (code == 200) {
                java.io.InputStream is = conn.getInputStream();
                String body = new String(is.readAllBytes());
                return body.contains("\"valid\":true");
            }
        } catch (Exception e) {
            // API unreachable — fall back to local key file
            SwingUtilities.invokeLater(() -> log("[Premium] Server unreachable, checking local cache..."));
            return loadPremiumKey();
        }
        return false;
    }

    private String showPremiumKeyDialog() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel msg = new JLabel("<html><b>Enter Premium Key</b><br><br>Enter your Aegis premium key below.<br>Don't have one? Email arhanh1234@gmail.com</html>");
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(12));

        JPasswordField keyField = new JPasswordField(20);
        keyField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(keyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Aegis Premium",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new String(keyField.getPassword());
        }
        return null;
    }

    // ==================== PREMIUM KEY PERSISTENCE ====================

    private Path getKeyFilePath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, KEY_FILE_NAME);
    }

    private boolean loadPremiumKey() {
        try {
            Path keyFile = getKeyFilePath();
            if (Files.exists(keyFile)) {
                String stored = Files.readString(keyFile).trim();
                return !stored.isEmpty();
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void savePremiumKey(String key) {
        try {
            Files.writeString(getKeyFilePath(), key);
        } catch (Exception e) {
            log("[Premium] Warning: Could not save key file: " + e.getMessage());
        }
    }

    // ==================== ACTIONS ====================

    private void autoInstallAegis() {
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

    private void refreshStatus() {
        boolean java = true; // we're running Java, so yes
        boolean minecraft = MinecraftPaths.getMinecraftDir().exists();
        boolean fabric = MinecraftPaths.isFabricInstalled();
        boolean fabricApi = MinecraftPaths.isFabricApiInstalled();
        boolean aegis = MinecraftPaths.isAegisInstalled();

        updateIndicator(statusJava, java, "Found", "Not Found");
        updateIndicator(statusMinecraft, minecraft, "Found", "Not Found");
        updateIndicator(statusFabric, fabric, "Installed", "Not Found");
        updateIndicator(statusFabricApi, fabricApi, "Installed", "Not Found");
        updateIndicator(statusAegis, aegis, "Installed", "Not Found");

        log("[Status] Java: Found");
        log("[Status] Minecraft: " + (minecraft ? "Found" : "NOT FOUND"));
        log("[Status] Fabric: " + (fabric ? "Installed" : "NOT FOUND"));
        log("[Status] Fabric API: " + (fabricApi ? "Installed" : "NOT FOUND"));
        log("[Status] Aegis: " + (aegis ? "Installed" : "NOT FOUND"));

        if (!fabric) {
            log("[!] Fabric Loader not found. Installing automatically...");
            downloadFabricInstaller();
        }
        if (!fabricApi) {
            log("[!] Fabric API not found. Installing automatically...");
            downloadFabricApi();
        }
        if (fabric && fabricApi && aegis) {
            log("[OK] Everything ready! Select edition and click Launch.");
        }
        log("");
    }

    private void onLaunch() {
        if (!MinecraftPaths.isFabricInstalled()) {
            log("[Launch] Fabric Loader not installed! Installing...");
            downloadFabricInstaller();
            return;
        }
        if (!MinecraftPaths.isFabricApiInstalled()) {
            log("[Launch] Fabric API not installed! Installing...");
            downloadFabricApi();
            return;
        }

        String username = usernameField.getText().trim();
        if (username.isEmpty()) username = "Player";
        int memory = (int) memorySpinner.getValue();

        String edition = premiumEdition ? "Premium" : "Free";
        log("[Launch] Launching (" + edition + ") with username: " + username + ", memory: " + memory + "MB");

        // If premium, install the premium JAR; otherwise install the free JAR
        if (premiumEdition) {
            installPremiumJar();
        }

        launchButton.setEnabled(false);
        log("[Launch] Minecraft Launcher opened!");
        log("[Launch] Select the \"Aegis Client\" profile and click Play.");

        new Thread(() -> {
            boolean success = gameLauncher.launchMinecraft();
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    log("[Launch] Minecraft opened!");
                } else {
                    log("[Launch] Launch failed. Check errors above.");
                }
                log("");
                launchButton.setEnabled(true);
            });
        }).start();
    }

    private void installPremiumJar() {
        // Look for premium JAR in premium-dist
        File jarDir = new File(System.getProperty("user.dir"));
        File[] premiumPaths = {
                new File(jarDir, "../premium-dist/aegis-client-premium-1.0.0.jar"),
                new File(jarDir, "premium-dist/aegis-client-premium-1.0.0.jar"),
                new File(jarDir.getParentFile(), "premium-dist/aegis-client-premium-1.0.0.jar"),
        };

        for (File path : premiumPaths) {
            if (path.exists()) {
                try {
                    gameLauncher.installAegisMod(path);
                    log("[Premium] Premium JAR installed.");
                } catch (Exception e) {
                    log("[Premium] Could not install premium JAR: " + e.getMessage());
                }
                return;
            }
        }

        // If no premium JAR found, the regular JAR should still work
        // (it has the marker resource if built from this repo)
        log("[Premium] Premium JAR not found in premium-dist/, using installed version.");
    }

    private void onInstall() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Aegis JAR");
        chooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));

        File buildLibs = new File(System.getProperty("user.dir"), "../build/libs");
        if (buildLibs.exists()) chooser.setCurrentDirectory(buildLibs);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            log("[Install] Installing " + selected.getName() + "...");

            new Thread(() -> {
                try {
                    gameLauncher.installAegisMod(selected);
                    SwingUtilities.invokeLater(() -> {
                        log("[Install] Success! Aegis installed.");
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

    private void downloadFabricInstaller() {
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
        new Thread(() -> {
            try {
                String apiUrl = "https://cdn.modrinth.com/data/P7dR8mSH/versions/tFw0iWAk/fabric-api-0.97.3%2B1.20.4.jar";
                File modsDir = MinecraftPaths.getModsDir();
                if (!modsDir.exists()) modsDir.mkdirs();

                File dest = new File(modsDir, "fabric-api-0.97.3+1.20.4.jar");

                try (InputStream in = new URL(apiUrl).openStream()) {
                    Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                SwingUtilities.invokeLater(() -> {
                    log("[Fabric API] Installed to mods folder!");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("[Fabric API] Download failed: " + e.getMessage());
                    try {
                        Desktop.getDesktop().browse(new URI("https://modrinth.com/mod/fabric-api/versions?g=1.20.4"));
                    } catch (Exception ignored) {}
                });
            }
            SwingUtilities.invokeLater(() -> log(""));
        }).start();
    }

    // ==================== UI HELPERS ====================

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
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createGoldButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new GradientPaint(0, 0, GOLD_DIM, getWidth(), 0, new Color(200, 140, 0));
                } else if (getModel().isRollover()) {
                    gp = new GradientPaint(0, 0, GOLD.brighter(), getWidth(), 0, new Color(255, 200, 40));
                } else {
                    gp = new GradientPaint(0, 0, GOLD, getWidth(), 0, new Color(255, 180, 0));
                }
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(BG_DARK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
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

    private JLabel createStatusDot() {
        JLabel label = new JLabel("\u25CF");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private JPanel createStatusRow(JLabel indicator, String name, boolean isInfo) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_PANEL);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_PANEL);
        left.add(indicator);
        JLabel label = new JLabel("  " + name);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        left.add(label);

        // Status text on the right
        JLabel statusText = new JLabel(isInfo ? "Found" : "Checking...");
        statusText.setForeground(TEXT);
        statusText.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusText.setName("statusText_" + name);

        row.add(left, BorderLayout.WEST);
        row.add(statusText, BorderLayout.EAST);

        return row;
    }

    private void updateIndicator(JLabel indicator, boolean installed, String trueText, String falseText) {
        indicator.setForeground(installed ? GREEN : RED);

        // Update the status text in the parent row
        Container parent = indicator.getParent();
        if (parent != null) {
            Container row = parent.getParent();
            if (row instanceof JPanel) {
                for (Component c : ((JPanel) row).getComponents()) {
                    if (c instanceof JLabel && ((JLabel) c).getName() != null && ((JLabel) c).getName().startsWith("statusText_")) {
                        ((JLabel) c).setText(installed ? trueText : falseText);
                        ((JLabel) c).setForeground(installed ? GREEN : RED);
                    }
                }
            }
        }
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
