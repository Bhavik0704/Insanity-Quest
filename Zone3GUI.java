import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Zone3GUI extends JPanel implements KeyListener, ActionListener {
    // Timer for game loop
    private Timer timer;

    // Player position and size
    private int playerX = 100;
    private int playerY = 400;
    private int playerWidth = 32;
    private int playerHeight = 32;
    private int dx = 0; // horizontal movement delta

    // Background and NPCs
    private Image backgroundImage;
    private Rectangle[] npcRects; // positions of NPCs
    private boolean[] interacted; // tracks whether NPCs have been interacted with

    // UI elements
    private JTextArea dialogueBox; // area to display dialogue
    private boolean interactionActive = false; // flag for whether dialogue or battle is in progress

    private JTextArea inventoryArea; // inventory UI (currently unused toggle)
    private boolean inventoryVisible = false;
    private JFrame parentFrame; // reference to the main frame for transitions

    // Fade transition elements
    private JPanel fadePanel; // overlay panel for fade effects
    private float fadeOpacity = 0f; // fade level: 0 = transparent, 1 = black
    private Timer fadeTimer; // timer to handle the fading animation
    private boolean fadingOut = false; // whether we are fading out to next zone

    public Zone3GUI(JFrame frame) {
        this.parentFrame = frame;

        // Set up main panel
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone3_background.jpg").getImage();
        } catch (Exception e) {
            System.err.println("Image loading failed.");
            e.printStackTrace();
        }

        // Initialize NPC rectangles and interaction tracker
        npcRects = new Rectangle[5];
        interacted = new boolean[5];
        for (int i = 0; i < 5; i++) {
            npcRects[i] = new Rectangle(150 + i * 120, 400, 30, 30);
        }

        // Setup dialogue box inside a scroll pane
        dialogueBox = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(dialogueBox);
        scrollPane.setBounds(100, 450, 600, 100);
        dialogueBox.setEditable(false);
        dialogueBox.setLineWrap(true);
        dialogueBox.setWrapStyleWord(true);
        dialogueBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(scrollPane);

        // Setup inventory area (optional feature)
        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        inventoryArea.setBounds(550, 50, 220, 150);
        inventoryArea.setBackground(new Color(255, 255, 255, 230));
        inventoryArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inventoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inventoryArea.setVisible(false);
        add(inventoryArea);

        // Setup panel used for fade transitions
        fadePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, fadeOpacity));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        fadePanel.setOpaque(false);
        fadePanel.setBounds(0, 0, 800, 600);
        fadePanel.setVisible(false);
        add(fadePanel);

        // Start the game loop
        timer = new Timer(16, this);
        timer.start();

        // Ensure keyboard focus
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerWidth, playerHeight);

        // Draw NPCs that have not yet been interacted with
        g.setColor(Color.RED);
        for (int i = 0; i < npcRects.length; i++) {
            if (!interacted[i]) {
                g.fillRect(npcRects[i].x, npcRects[i].y, npcRects[i].width, npcRects[i].height);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle player movement
        playerX += dx;
        playerX = Math.max(0, Math.min(getWidth() - playerWidth, playerX));

        // If player reaches the end of the screen, transition to next zone
        if (playerX >= getWidth() - playerWidth) {
            timer.stop();
            parentFrame.getContentPane().removeAll();
            parentFrame.getContentPane().add(new Zone4GUI(parentFrame));
            parentFrame.revalidate();
            parentFrame.repaint();
        }

        repaint(); // request UI update
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (interactionActive) return; // disable movement during interaction

        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_LEFT -> dx = -5;
            case KeyEvent.VK_RIGHT -> dx = 5;
            case KeyEvent.VK_SPACE -> checkForInteraction(); // spacebar triggers interactions
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Stop movement when arrow keys released
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {} // unused

    // Check for interaction with any NPC
    private void checkForInteraction() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (int i = 0; i < npcRects.length; i++) {
            if (!interacted[i] && playerRect.intersects(npcRects[i])) {
                interacted[i] = true;
                interactionActive = true;

                if (i == 4) {
                    // Interaction with "Frog" (key NPC)
                    String frogDialogue = "???: You don’t seem quite like the others...\n" +
                            "You: And that’s because I’m not\n" +
                            "???: So you are still sane? How did you get here? Where did you come from? Did you lead anything here?\n" +
                            "You: I just got here and am trying to reach the more sane people.\n" +
                            "???: You shouldn’t be here. Everyone still sane was evacuated.\n" +
                            "You: Then why are you here?\n" +
                            "???: Isn’t that obvious? I’m doing research on what these things have become, what actually turned people insane.\n" +
                            "You: So where do I go?\n" +
                            "???: Just follow me before you get yourself killed, though you seem fairly powerful.\n" +
                            "You: So who are you?\n" +
                            "Frog: Just call me Frog for now.";

                    // Display Frog's dialogue, then fade out to next zone
                    typeText(frogDialogue, () -> {
                        interactionActive = false;
                        timer.stop();
                        startFadeTransition();
                    });

                } else {
                    // Hostile NPC triggers battle
                    typeText("This NPC seems hostile! Starting battle...", () -> {
                        this.setVisible(false);
                        new BattleSystem2("hard", () -> {
                            SwingUtilities.invokeLater(() -> {
                                this.setVisible(true);
                                interactionActive = false;
                                dialogueBox.setText("You defeated the hostile NPC. Your stats have been restored.");
                                requestFocusInWindow();
                                repaint();
                            });
                        });
                    });
                }
                break;
            }
        }
    }

    // Handles fading out and transitioning to Zone4
    private void startFadeTransition() {
        fadePanel.setVisible(true);
        fadeOpacity = 0f;
        fadingOut = true;

        fadeTimer = new Timer(40, e -> {
            if (fadingOut) {
                fadeOpacity += 0.05f;
                if (fadeOpacity >= 1f) {
                    fadeOpacity = 1f;
                    fadeTimer.stop();

                    // Switch to Zone4 on EDT
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.getContentPane().removeAll();
                        parentFrame.getContentPane().add(new Zone4GUI(parentFrame));
                        parentFrame.revalidate();
                        parentFrame.repaint();

                        // Start fade-in after transition
                        fadeTimer = new Timer(40, e2 -> {
                            fadeOpacity -= 0.05f;
                            if (fadeOpacity <= 0f) {
                                fadeOpacity = 0f;
                                fadePanel.setVisible(false);
                                ((Timer) e2.getSource()).stop();
                            }
                            fadePanel.repaint();
                        });
                        fadeTimer.start();
                    });
                }
            }
            fadePanel.repaint();
        });
        fadeTimer.start();
    }

    // Typewriter effect for displaying dialogue, with optional callback when complete
    private void typeText(String fullText, Runnable onComplete) {
        dialogueBox.setText("");
        final Timer typingTimer = new Timer(30, null);
        final int[] index = {0};

        typingTimer.addActionListener(e -> {
            if (index[0] < fullText.length()) {
                dialogueBox.append(String.valueOf(fullText.charAt(index[0]++)));
            } else {
                typingTimer.stop();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        typingTimer.start();
    }

    // Overloaded method for typeText with no completion callback
    private void typeText(String fullText) {
        typeText(fullText, null);
    }

    // Main method for standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zone 3");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new Zone3GUI(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
