import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class Zone4GUI extends JPanel implements KeyListener, ActionListener {

    // Main game frame reference
    private JFrame parentFrame;

    // Timer for game loop
    private Timer timer;

    // Player position and movement
    private int playerX = 100;
    private int playerY = 492;
    private final int playerRadius = 32; // Diameter of player sprite
    private int dx = 0; // Horizontal movement delta

    // Background image
    private Image backgroundImage;

    // Map of NPC names to their position rectangles
    private HashMap<String, Rectangle> npcMap = new HashMap<>();

    // Tracks which NPCs the player has interacted with
    private HashMap<String, Boolean> npcInteracted = new HashMap<>();

    public Zone4GUI(JFrame frame) {
        this.parentFrame = frame;

        // Basic panel setup
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone4_background.png").getImage();
        } catch (Exception e) {
            System.err.println("Background image not found.");
        }

        // Define only one NPC: the miniboss
        npcMap.put("Miniboss", new Rectangle(600, 492, 32, 32));
        npcInteracted.put("Miniboss", false);

        // Start the game loop timer
        timer = new Timer(16, this); // roughly 60 FPS
        timer.start();

        // Ensure the panel has focus to receive key events
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image if available
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback to solid color background
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw the player character
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerRadius, playerRadius);

        // Draw miniboss NPC
        Rectangle rect = npcMap.get("Miniboss");
        g.setColor(Color.MAGENTA);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update player position and clamp within screen bounds
        playerX += dx;
        playerX = Math.max(0, Math.min(getWidth() - playerRadius, playerX));

        // Repaint screen
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Handle movement and interaction keys
        switch (key) {
            case KeyEvent.VK_LEFT -> dx = -5;
            case KeyEvent.VK_RIGHT -> dx = 5;
            case KeyEvent.VK_SPACE -> checkForInteraction();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Stop movement when arrow keys are released
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // Check for interaction with NPCs
    private void checkForInteraction() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerRadius, playerRadius);
        Rectangle minibossRect = npcMap.get("Miniboss");

        // Trigger interaction with miniboss if overlapping and not already interacted
        if (playerRect.intersects(minibossRect) && !npcInteracted.get("Miniboss")) {
            npcInteracted.put("Miniboss", true);
            interactWithMiniboss();
        }
    }

    // Handle miniboss interaction (battle and zone transition)
    private void interactWithMiniboss() {
        // Disable this panel and hide the frame
        setEnabled(false);
        parentFrame.setVisible(false);

        // Launch 2v1 battle system with callback
        new BattleSystem2v1(() -> {
            // After victory: launch Zone5GUI in a new frame
            SwingUtilities.invokeLater(() -> {
                parentFrame.dispose();

                JFrame zone5Frame = new JFrame("Zone 5");
                zone5Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Zone5GUI zone5Panel = new Zone5GUI(zone5Frame);
                zone5Frame.setContentPane(zone5Panel);

                zone5Frame.pack();
                zone5Frame.setLocationRelativeTo(null);
                zone5Frame.setVisible(true);
            });
        });
    }

    // Main method to test Zone4GUI independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zone 4");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Zone4GUI zone4Panel = new Zone4GUI(frame);
            frame.setContentPane(zone4Panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
