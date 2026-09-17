import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class Zone6GUI extends JPanel implements KeyListener, ActionListener {
    // Timer for movement and animation
    private Timer timer;

    // Player position and size
    private int playerX = 50;
    private int playerY = 500;
    private int playerRadius = 32;
    private int dx = 0; // horizontal movement delta

    // Visuals and frame reference
    private Image backgroundImage;
    private JFrame parentFrame;

    // Dialogue display components
    private JTextArea dialogueBox;
    private JScrollPane scrollPane;

    // Important map elements
    private Rectangle bossZone = new Rectangle(700, 460, 80, 80); // Zone that starts the boss fight
    private Rectangle npcBeforeBoss = new Rectangle(600, 500, 32, 32); // NPC that gives pre-battle dialogue

    // State flags
    private boolean interactedWithPreBossNPC = false; // Has the player spoken to the NPC
    private boolean triggeredPreBattleDialogue = false; // Has the post-dialogue started
    private boolean bossBattleStarted = false; // Has the boss battle been triggered
    private boolean postDialogueFinished = false; // Has the dialogue before the boss battle completed

    // Music
    private Clip backgroundMusic;

    // Constructor
    public Zone6GUI(JFrame frame) {
        this.parentFrame = frame;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); // use absolute positioning for dialogue box

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone6_background.jpg").getImage();
        } catch (Exception e) {
            System.err.println("Background image not found.");
        }

        // Set up dialogue box
        dialogueBox = new JTextArea();
        scrollPane = new JScrollPane(dialogueBox);
        scrollPane.setBounds(100, 20, 600, 100);
        dialogueBox.setEditable(false);
        dialogueBox.setLineWrap(true);
        dialogueBox.setWrapStyleWord(true);
        dialogueBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(scrollPane);

        // Play music for Zone 6
        playMusic("resources/zone6_journey.wav");

        // Start main game timer
        timer = new Timer(16, this); // roughly 60 FPS
        timer.start();

        // Ensure keyboard focus
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // Method to play looping background music
    private void playMusic(String filepath) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(filepath));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInput);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    // Draw game elements
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerRadius, playerRadius);

        // Draw pre-boss NPC
        g.setColor(Color.MAGENTA);
        g.fillRect(npcBeforeBoss.x, npcBeforeBoss.y, npcBeforeBoss.width, npcBeforeBoss.height);

        // Draw boss zone (dark gray background with two red "eyes")
        g.setColor(Color.DARK_GRAY);
        g.fillRect(bossZone.x, bossZone.y, bossZone.width, bossZone.height);
        g.setColor(Color.RED);
        g.fillOval(bossZone.x + 10, bossZone.y + 10, 15, 15);
        g.fillOval(bossZone.x + 55, bossZone.y + 10, 15, 15);
    }

    // Game loop update logic
    @Override
    public void actionPerformed(ActionEvent e) {
        // Move player within bounds
        playerX += dx;
        playerX = Math.max(0, Math.min(getWidth() - playerRadius, playerX));

        Rectangle playerRect = new Rectangle(playerX, playerY, playerRadius, playerRadius);

        // Interact with pre-boss NPC
        if (!interactedWithPreBossNPC && playerRect.intersects(npcBeforeBoss)) {
            interactedWithPreBossNPC = true;
            triggerPreBattleDialogue();
        }

        // Trigger dialogue near boss zone
        if (interactedWithPreBossNPC && playerRect.intersects(bossZone) && !triggeredPreBattleDialogue) {
            triggeredPreBattleDialogue = true;
            showPostDialogue();
        }

        // Start boss battle after dialogue is complete
        if (triggeredPreBattleDialogue && postDialogueFinished && !bossBattleStarted && playerRect.intersects(bossZone)) {
            bossBattleStarted = true;
            if (backgroundMusic != null) backgroundMusic.stop();
            SwingUtilities.invokeLater(() -> {
                parentFrame.dispose(); // Close the current zone
                new Zone6BossPhase1().setVisible(true); // Start boss phase
            });
        }

        repaint();
    }

    // Dialogue when interacting with pre-boss NPC
    private void triggerPreBattleDialogue() {
        String text = """
                Rigurd: Today is the day, a day of triumph, a day of victory. With all the training that we’ve done, and all the strength that we have built up I have no doubt that we will succeed.
                ???: Rigurd said it best, we leave at dawn, so pack up and get ready to go.

                It really is difficult to believe that this is an area in a world of insanity. I suppose I should just accept what it has come down to and forge ahead. I still don’t know her (???) name … And why does it look like she’s not even there?

                When you finally make it into the next area, the mood shifts significantly. Everyone becomes alert and starts following Rigurd and ??? with care. There are a few people that you meet along the way who look like enlightened souls, each of them warns you about what lies ahead. Honestly speaking, you all knew what to expect, so this was no surprise for you.
                """;
        typeText(text);
    }

    // Final dialogue before the boss encounter
    private void showPostDialogue() {
        String text = """
                You: I feel something ahead, in that building over there.
                Rigurd: That seems to be where the boss always rests, I’m not sure I can call it a boss after seeing it firsthand though.
                Caroline: What does that mean?
                Rigurd: Just remember what we trained you for.
                Everyone: Understood.
                """;
        typeTextWithCallback(text, () -> postDialogueFinished = true);
    }

    // Animate dialogue typing with optional callback
    private void typeText(String fullText) {
        typeTextWithCallback(fullText, null);
    }

    private void typeTextWithCallback(String fullText, Runnable callback) {
        dialogueBox.setText("");
        final Timer typingTimer = new Timer(30, null);
        final int[] index = {0};

        typingTimer.addActionListener(e -> {
            if (index[0] < fullText.length()) {
                dialogueBox.append(String.valueOf(fullText.charAt(index[0]++)));
                dialogueBox.setCaretPosition(dialogueBox.getDocument().getLength());
            } else {
                typingTimer.stop();
                if (callback != null) callback.run(); // run callback after typing completes
            }
        });
        typingTimer.start();
    }

    // Handle key inputs for movement
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) dx = -5;
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = 5;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) dx = 0;
    }

    @Override public void keyTyped(KeyEvent e) {}

    // Main method for testing this zone independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zone 6");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new Zone6GUI(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
