import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Zone1GUI extends JPanel implements ActionListener, KeyListener {

    // Timer for game loop (runs every ~16ms for ~60 FPS)
    private Timer timer;

    // Player attributes
    private int playerX = 100;
    private int playerY = 470; // Slightly lowered position
    private int playerSize = 30;

    // Background
    private Image backgroundImage;

    // Flags for transitions and interactions
    private boolean showTransition = false;
    private boolean battleStarted = false;
    private boolean zone2TransitionStarted = false;

    // NPCs
    private ArrayList<Rectangle> passiveNPCs = new ArrayList<>(); // List of friendly/passive NPCs
    private Set<Rectangle> interactedNPCs = new HashSet<>();      // Tracks which NPCs have been interacted with
    private Rectangle hostileNPC;                                 // Single hostile NPC

    // UI Components
    private JTextArea dialogueBox;     // Dialogue box for showing messages
    private Timer typingTimer;         // Timer for typewriter effect
    private String fullMessage = "";   // Full message to show
    private int charIndex = 0;         // Index for typewriter effect

    private JTextArea inventoryArea;   // Optional inventory display (not interacted with in this zone)
    private boolean inventoryVisible = false;

    // Constructor
    public Zone1GUI() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        setLayout(null); // Allows absolute positioning
        addKeyListener(this);

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone1_background_mockup.png").getImage();
        } catch (Exception e) {
            System.err.println("Background loading failed.");
        }

        // Initialize hostile NPC and passive NPCs
        hostileNPC = new Rectangle(400, 470, 30, 30);
        passiveNPCs.add(new Rectangle(250, 470, 30, 30));
        passiveNPCs.add(new Rectangle(550, 470, 30, 30));

        // Inventory setup (though not used visibly in Zone 1)
        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        inventoryArea.setBounds(550, 50, 220, 150);
        inventoryArea.setBackground(new Color(255, 255, 255, 230));
        inventoryArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inventoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inventoryArea.setVisible(false); // Hidden by default
        add(inventoryArea);

        // Dialogue box setup
        dialogueBox = new JTextArea();
        dialogueBox.setEditable(false);
        dialogueBox.setLineWrap(true);
        dialogueBox.setWrapStyleWord(true);
        dialogueBox.setFont(new Font("Monospaced", Font.PLAIN, 16));
        dialogueBox.setBounds(50, 500, 700, 60);
        add(dialogueBox);

        // Start the timer loop
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    // Drawing logic
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerSize, playerSize);

        // Draw passive NPCs
        g.setColor(Color.RED);
        for (Rectangle npc : passiveNPCs) {
            g.fillOval(npc.x, npc.y, npc.width, npc.height);
        }

        // Draw hostile NPC if present
        if (hostileNPC != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(hostileNPC.x, hostileNPC.y, hostileNPC.width, hostileNPC.height);
        }

        // Fade/transition overlay
        if (showTransition) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Transitioning to Battle...", 220, 300);
        }
    }

    // Game logic - every tick
    @Override
    public void actionPerformed(ActionEvent e) {
        // If player intersects hostile NPC, trigger battle
        if (hostileNPC != null && new Rectangle(playerX, playerY, playerSize, playerSize).intersects(hostileNPC)) {
            hostileNPC = null; // Remove hostile NPC
            triggerBattle();
        }

        // If player reaches far right, transition to Zone 2
        if (playerX >= getWidth() - playerSize && !zone2TransitionStarted) {
            zone2TransitionStarted = true;
            transitionToZone2();
            return;
        }

        repaint(); // Redraw everything
    }

    // Zone transition logic
    private void transitionToZone2() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // Close current window

        SwingUtilities.invokeLater(() -> {
            JFrame zone2Frame = new JFrame("Zone 2");
            zone2Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            zone2Frame.setContentPane(new Zone2GUI());
            zone2Frame.pack();
            zone2Frame.setLocationRelativeTo(null);
            zone2Frame.setVisible(true);
        });
    }

    // Trigger battle screen
    private void triggerBattle() {
        if (!battleStarted) {
            battleStarted = true;
            showTransition = true;

            // Delay transition for 2 seconds for visual effect
            Timer transitionTimer = new Timer(2000, evt -> {
                SwingUtilities.getWindowAncestor(this).dispose();
                SwingUtilities.invokeLater(() -> new BattleSystem(() -> {
                    // Callback after battle ends
                    Zone1GUI newZone = new Zone1GUI();
                    newZone.setPlayerPositionAfterBattle();
                    JFrame frame = new JFrame("Zone 1");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setContentPane(newZone);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }));
            });
            transitionTimer.setRepeats(false);
            transitionTimer.start();
        }
    }

    // Reset player position after battle concludes
    public void setPlayerPositionAfterBattle() {
        playerX = 450;
        playerY = 470;
    }

    // Keyboard Input
    @Override
    public void keyPressed(KeyEvent e) {
        if (battleStarted) return; // Disable movement during transition

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                playerX = Math.max(0, playerX - 5);
                break;
            case KeyEvent.VK_RIGHT:
                playerX = Math.min(getWidth() - playerSize, playerX + 5);
                break;
        }

        // Check passive NPC interactions
        Rectangle playerRect = new Rectangle(playerX, playerY, playerSize, playerSize);
        for (int i = 0; i < passiveNPCs.size(); i++) {
            Rectangle npc = passiveNPCs.get(i);
            if (npc.intersects(playerRect) && !interactedNPCs.contains(npc)) {
                interactedNPCs.add(npc);
                if (i == 0) {
                    // First NPC gives tutorial hint
                    showDialogue("Narrator: After this area, you will need to press SPACE in order to interact. \nYou will also make decisions using the numbers 1 or 2.");
                } else {
                    // Second NPC is incoherent
                    showDialogue("D:{WDKJIMASD><>?AIJWD!@Z (You can't understand)");
                }
            }
        }

        repaint();
    }

    // Dialogue with typewriter effect
    private void showDialogue(String message) {
        if (typingTimer != null && typingTimer.isRunning()) typingTimer.stop();

        fullMessage = message;
        charIndex = 0;
        dialogueBox.setText("");

        typingTimer = new Timer(30, e -> {
            if (charIndex < fullMessage.length()) {
                dialogueBox.append(String.valueOf(fullMessage.charAt(charIndex++)));
            } else {
                typingTimer.stop();
            }
        });
        typingTimer.start();
    }

    // Unused KeyListener methods
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Entry Point for Testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zone 1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new Zone1GUI());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
