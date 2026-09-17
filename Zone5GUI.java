
import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Zone5GUI extends JPanel implements KeyListener, ActionListener {
    // Timer for movement updates
    private Timer timer;

    // Player properties
    private int playerX = 100;
    private int playerY = 492;
    private int playerRadius = 32;
    private int dx = 0;

    // Background image and parent frame reference
    private Image backgroundImage;
    private JFrame parentFrame;

    // Dialogue UI components
    private JTextArea dialogueBox;
    private JScrollPane scrollPane;

    // Map of all NPCs and interaction flags
    private HashMap<String, Rectangle> npcMap = new HashMap<>();
    private HashMap<String, Boolean> npcInteracted = new HashMap<>();

    // Flag for bed prompt
    private boolean sleepPromptActive = false;

    // Background music
    private Clip bgmClip;

    // Fade effect components
    private JPanel fadePanel;
    private float fadeOpacity = 0f;
    private Timer fadeTimer;

    public Zone5GUI(JFrame frame) {
        this.parentFrame = frame;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone5_background.jpg").getImage();
        } catch (Exception e) {
            System.err.println("Background image not found.");
        }

        // Initialize NPC positions
        npcMap.put("Rigurd", new Rectangle(250, 492, 32, 32));
        npcMap.put("Caroline", new Rectangle(350, 492, 32, 32));
        npcMap.put("Darius", new Rectangle(450, 492, 32, 32));
        npcMap.put("???", new Rectangle(550, 492, 32, 32));
        npcMap.put("Bed", new Rectangle(700, 492, 32, 32));

        // Mark all NPCs as not yet interacted
        for (String key : npcMap.keySet()) {
            npcInteracted.put(key, false);
        }

        // Create and add dialogue box
        dialogueBox = new JTextArea();
        scrollPane = new JScrollPane(dialogueBox);
        scrollPane.setBounds(100, 20, 600, 100); // Positioned at top of screen
        dialogueBox.setEditable(false);
        dialogueBox.setLineWrap(true);
        dialogueBox.setWrapStyleWord(true);
        dialogueBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(scrollPane);

        // Fade overlay panel
        fadePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, Math.min(1f, fadeOpacity)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        fadePanel.setOpaque(false);
        fadePanel.setBounds(0, 0, 800, 600);
        add(fadePanel);

        // Start movement timer
        timer = new Timer(16, this);
        timer.start();

        // Ensure key focus
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        // Play zone music
        playBackgroundMusic("resources/zone5_music.wav");
    }

    // Play looping background music
    private void playBackgroundMusic(String filepath) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(filepath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInput);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    // Stop music playback
    private void stopBackgroundMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    // Draw game elements (background, player, NPCs)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerRadius, playerRadius);

        // Draw NPCs with color coding
        for (String key : npcMap.keySet()) {
            Rectangle rect = npcMap.get(key);
            switch (key) {
                case "???" -> g.setColor(Color.BLACK);
                case "Mystery" -> g.setColor(Color.BLACK);
                case "Bed" -> g.setColor(Color.GREEN);
                default -> g.setColor(Color.RED);
            }
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    // Update player position each frame
    @Override
    public void actionPerformed(ActionEvent e) {
        playerX += dx;
        playerX = Math.max(0, Math.min(getWidth() - playerRadius, playerX));
        repaint();
    }

    // Handle player input
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (sleepPromptActive) {
            if (key == KeyEvent.VK_1) {
                sleepPromptActive = false;
                typeText("You decided to sleep... The day ends.");
                startFadeTransitionToZone6();
            } else if (key == KeyEvent.VK_2) {
                sleepPromptActive = false;
                typeText("You decided not to sleep now.");
            }
            return;
        }

        switch (key) {
            case KeyEvent.VK_LEFT -> dx = -5;
            case KeyEvent.VK_RIGHT -> dx = 5;
            case KeyEvent.VK_SPACE -> checkForInteraction();
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) dx = 0;
    }
    @Override public void keyTyped(KeyEvent e) {}

    // Check if player is near an NPC and trigger interaction
    private void checkForInteraction() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerRadius, playerRadius);
        for (String key : npcMap.keySet()) {
            Rectangle npc = npcMap.get(key);
            if (playerRect.intersects(npc) && !npcInteracted.get(key)) {
                npcInteracted.put(key, true);
                if (key.equals("Bed")) {
                    sleepPromptActive = true;
                    typeText("Would you like to sleep now? (1 - Yes, 2 - No)");
                } else {
                    showDialogue(key);
                }
                break;
            }
        }
    }

    // Show dialogue text for each NPC
    private void showDialogue(String npcName) {
        String text = switch (npcName) {
            case "Rigurd" -> "Rigurd: Hey, you look exhausted. If it’s alright with you, we can offer you a bed and maybe even group to venture with moving forward."
                    + "\nYou: Are you sure?\nRigurd: Absolutely. Guys lets help him out.";
            case "Caroline" -> "Caroline: You know we actually all came here at different times, and it wasn’t always this happy and joyful."
                    + "\nYou: How long have you been here then?"
                    + "\nCaroline: I’m actually one of the newer people here arriving about two days ago. Rigurd also welcomed me with open arms and a bright smile."
                    + "\nYou: So he really is just a good guy."
                    + "\nCaroline: I think so too, but its just I can’t help but wonder in a place of insanity why Rigurd is able to smile as he does."
                    + "\nYou: I suppose that makes sense. But I think we should just focus on the future."
                    + "\nCaroline: That we should!";
            case "Darius" -> "Darius: You seem like you have gone through a great horror along the way…"
                    + "\nYou: Why would you say that?"
                    + "\nDarius: There is a completely different demeanor about you, especially compared to the others. Then again that’s not why I called you here."
                    + "\nYou: Then what happened?"
                    + "\nDarius: I felt the need to tell you something. About what’s happening here."
                    + "\nYou: Isn’t this just a sort of rest area?"
                    + "\nDarius: That’s what you would think. Before Caroline got here I was the third, but when I got here Rigurd and ??? were just there with little to no emotion using each other to comfort themselves."
                    + "\nYou: Then how did he come to this point?"
                    + "\nDarius: With my help. These two were rather tired, and they had come back from the 1-9% insanity zone. All they could do was look up in despair. Once they saw me, it’s almost as if a glimmer of hope overtook them."
                    + "\nYou: And what did you do?"
                    + "\nDarius: I did what any man would’ve done, lend them a hand. I built them a bed and allowed them to rest while almost being a therapist."
                    + "\nYou: That must’ve been hard on you though was it not?"
                    + "\nDarius: Of course, but you cannot overcome yourself without overcoming the hardships sent at you first."
                    + "\nYou: That might be the greatest mindset I’ve seen."
                    + "\nDarius: Why thank you, but it is getting late now so I suggest we get to bed.";
            case "???" -> "Theres something I must do, somewhere, somehow, I MUST.";
            default -> "";
        };
        typeText(text);
    }

    // Typewriter-style dialogue output
    private void typeText(String fullText) {
        dialogueBox.setText("");
        final Timer typingTimer = new Timer(30, null);
        final int[] index = {0};

        typingTimer.addActionListener(e -> {
            if (index[0] < fullText.length()) {
                dialogueBox.append(String.valueOf(fullText.charAt(index[0]++)));
                dialogueBox.setCaretPosition(dialogueBox.getDocument().getLength());
            } else {
                typingTimer.stop();
            }
        });
        typingTimer.start();
    }

    // Start fade transition and switch to Zone6GUI
    private void startFadeTransitionToZone6() {
        dx = 0; // Stop player movement
        fadeOpacity = 0f;

        fadeTimer = new Timer(40, null);
        fadeTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeOpacity += 0.05f;
                fadePanel.repaint();
                if (fadeOpacity >= 1f) {
                    fadeTimer.stop();
                    stopBackgroundMusic();
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.getContentPane().removeAll();
                        parentFrame.getContentPane().add(new Zone6GUI(parentFrame));
                        parentFrame.revalidate();
                        parentFrame.repaint();
                    });
                }
            }
        });
        fadeTimer.start();
    }

    // Launch Zone 5 window
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zone 5");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new Zone5GUI(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
