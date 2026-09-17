import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Zone2GUI extends JPanel implements KeyListener, ActionListener {

    // Main game loop timer
    private Timer timer;

    // Player position and size
    private int playerX = 100;
    private int playerY = 400;
    private int playerSize = 30;

    // Background image for the zone
    private Image backgroundImage;

    // Dialogue display area
    private JTextArea dialogueBox;

    // Track completion of each of the 5 trials
    private boolean[] trialCompleted = new boolean[5];

    // Counter for how many trials have been successfully passed
    private int trialsPassed = 0;

    // Array holding the rectangles representing trial NPCs
    private Rectangle[] trialNPCs = new Rectangle[5];

    // Flags to manage interaction and trial state
    private boolean interactionActive = false;
    private boolean trialActive = false;
    private int currentTrialIndex = -1;  // Currently active trial index
    private String[] currentOptions = null;  // Current options presented to player
    private int currentChoice = -1;  // Player's selected option (0 or 1)

    // Player's attributes
    private int playerHealth = 100;
    private int playerSanity = 100;

    // Optional inventory display
    private JTextArea inventoryArea;
    private boolean inventoryVisible = false;

    public Zone2GUI() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); // Absolute layout for custom component placement

        // Load background image
        try {
            backgroundImage = new ImageIcon("resources/zone2_background.png").getImage();
        } catch (Exception e) {
            System.err.println("Zone 2 background loading failed.");
        }

        // Set up inventory area (optional feature)
        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        inventoryArea.setBounds(550, 50, 220, 150);
        inventoryArea.setBackground(new Color(255, 255, 255, 230));
        inventoryArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inventoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inventoryArea.setVisible(false);
        add(inventoryArea);

        // Position each of the 5 trial NPCs across the screen
        for (int i = 0; i < 5; i++) {
            trialNPCs[i] = new Rectangle(150 + i * 120, 400, 30, 30);
        }

        // Create and configure dialogue box
        dialogueBox = new JTextArea();
        dialogueBox.setBounds(100, 450, 600, 100);
        dialogueBox.setEditable(false);
        dialogueBox.setLineWrap(true);
        dialogueBox.setWrapStyleWord(true);
        dialogueBox.setFont(new Font("Arial", Font.PLAIN, 14));
        add(dialogueBox);

        // Start the repaint loop
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, playerSize, playerSize);

        // Draw uncompleted trial NPCs as magenta rectangles
        g.setColor(Color.MAGENTA);
        for (int i = 0; i < 5; i++) {
            if (!trialCompleted[i]) {
                g.fillRect(trialNPCs[i].x, trialNPCs[i].y, trialNPCs[i].width, trialNPCs[i].height);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Handle trial choice selection
        if (trialActive) {
            if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
                currentChoice = 0;
                processTrialChoice();
            } else if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
                currentChoice = 1;
                processTrialChoice();
            }
            return;
        }

        // Prevent movement while interacting
        if (interactionActive) return;

        // Basic player movement and interaction
        switch (key) {
            case KeyEvent.VK_LEFT -> playerX = Math.max(0, playerX - 5);
            case KeyEvent.VK_RIGHT -> playerX = Math.min(getWidth() - playerSize, playerX + 5);
            case KeyEvent.VK_SPACE -> checkForNPCInteraction(); // Start dialogue/trial if touching NPC
        }

        repaint();
    }

    // Checks if player is colliding with a trial NPC and initiates a trial
    private void checkForNPCInteraction() {
        if (interactionActive || trialActive) return;

        Rectangle playerRect = new Rectangle(playerX, playerY, playerSize, playerSize);

        for (int i = 0; i < trialNPCs.length; i++) {
            if (!trialCompleted[i] && trialNPCs[i].intersects(playerRect)) {
                interactionActive = true;
                handleTrial(i);
                return;
            }
        }

        dialogueBox.setText("");
    }

    // Processes result of player's trial choice
    private void processTrialChoice() {
        int idx = currentTrialIndex;
        boolean passed = false;

        switch (idx) {
            case 0 -> passed = (currentChoice == 0); // Mentality
            case 1 -> { // Trust
                boolean easyBattle = (currentChoice == 1); // 1 = battle, 0 = conversation
                dialogueBox.setText("You chose: " + currentOptions[currentChoice] + "\nStarting battle...");
                startBattleForTrustTrial(easyBattle);
                return;
            }
            case 2 -> passed = (currentChoice == 0); // Goodwill
            case 3 -> passed = (currentChoice == 1); // Identity
            case 4 -> { // Sacrifice
                dialogueBox.setText("You chose: " + currentOptions[currentChoice] + ". The weight affects your sanity...");
                passed = true; // Always passes, but with implications
            }
        }

        dialogueBox.setText("You chose: " + currentOptions[currentChoice]);
        finishTrial(idx, passed);
    }

    // Starts a battle with difficulty depending on the Trust trial outcome
    private void startBattleForTrustTrial(boolean easyBattle) {
        interactionActive = true;
        trialActive = true;
        this.setVisible(false); // Hide Zone2 GUI during battle

        new BattleSystem2(easyBattle ? "easy" : "hard", () -> {
            SwingUtilities.invokeLater(() -> {
                this.setVisible(true);
                interactionActive = false;
                trialActive = false;
                finishTrial(1, true); // Always pass the trust trial after battle
                dialogueBox.setText("You passed the Trial of Trust!");
                repaint();
            });
        });
    }

    // Handles dialogue and setup for the specific trial selected
    private void handleTrial(int index) {
        if (trialActive) return;

        trialActive = true;
        currentTrialIndex = index;
        currentChoice = -1;

        switch (index) {
            case 0 -> startTrial("Trial of Mentality:\nWhy do you truly wish to move forward, why not just fade into a peaceful life?",
                    new String[]{
                            "1) The peaceful life may be one that people yearn for, but what I seek lies further",
                            "2) There may not be a set path, but still the mind should hold enough power to push for more"
                    });
            case 1 -> startTrial("Trial of Trust:\nIf given the choice between a battle and a conversation with a stranger, what would you pursue?",
                    new String[]{
                            "1) I would choose a conversation with a stranger",
                            "2) I would prepare for battle"
                    });
            case 2 -> startTrial("Trial of Goodwill:\nGiven a child starving on the brink of collapse, how would you react?",
                    new String[]{
                            "1) Tend to the child and offer them something",
                            "2) Leave the child alone exposing them to the nature of humanity"
                    });
            case 3 -> startTrial("Trial of Identity:\nWhen left without power, fame, or wealth, what are you?",
                    new String[]{
                            "1) I am what my challenges have given me, there is little without growth",
                            "2) I am the choices that I make, even when nobody can see them"
                    });
            case 4 -> startTrial("Trial of Sacrifice:\nTwo are in danger: a cherished one and a stranger. Who do you save?",
                    new String[]{
                            "1) Save the one I've cherished",
                            "2) Save the complete stranger"
                    });
        }
    }

    // Displays trial question and options
    private void startTrial(String question, String[] options) {
        dialogueBox.setText(question + "\n\n" + String.join("\n", options) + "\n\nPress 1 or 2 to choose.");
        currentOptions = options;
    }

    // Finalizes trial state and transitions if applicable
    private void finishTrial(int index, boolean passed) {
        trialCompleted[index] = true;
        if (passed) trialsPassed++;

        interactionActive = false;
        trialActive = false;

        this.requestFocusInWindow(); // Re-grab key focus

        // Transition to Zone 3 or end game based on trial results
        if (allTrialsCompleted()) {
            if (trialsPassed >= 3) {
                dialogueBox.setText("You have passed enough trials to proceed to Zone 3!");
                Timer transitionTimer = new Timer(3000, evt -> {
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    topFrame.setContentPane(new Zone3GUI(topFrame));
                    topFrame.revalidate();
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();
            } else {
                dialogueBox.setText("You have failed too many trials. Your journey ends here.");
            }
        }

        repaint();
    }

    // Utility function to check whether all trials have been interacted with
    private boolean allTrialsCompleted() {
        for (boolean b : trialCompleted) if (!b) return false;
        return true;
    }

    // Optional helper to move player near trial NPC
    private void respawnPlayerNear(int index) {
        playerX = trialNPCs[index].x + 50;
        playerY = trialNPCs[index].y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint(); // Continuous repaint loop
    }

    // Unused key event methods
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
