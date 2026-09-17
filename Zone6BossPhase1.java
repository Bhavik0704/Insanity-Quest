import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Zone6BossPhase1 extends JFrame {

    // Progress bars for player and boss status
    private JProgressBar playerHealthBar;
    private JProgressBar playerSanityBar;
    private JProgressBar bossHealthBar;

    // Text area for animated dialogue display
    private JTextArea dialogueArea;
    private JScrollPane dialogueScrollPane;

    // Buttons for combat actions
    private JButton attackButton, skillButton, defendButton;

    // Player and boss health/sanity stats
    private int playerHealth = 100;
    private int playerSanity = 100;
    private int bossHealth = 75;

    // Flag to indicate if the player is currently defending
    private boolean defending = false;

    // Variables for typewriter effect
    private Timer typewriterTimer;
    private int typewriterIndex;
    private String fullTextToAnimate;

    // Constructor to initialize the boss phase 1 window
    public Zone6BossPhase1() {
        setTitle("Zone 6 - Boss Phase 1"); // Window title
        setSize(800, 600); // Window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit on close
        setLayout(null); // Use absolute positioning
        setupUI(); // Setup the visual interface
        setVisible(true); // Make the window visible
    }

    // Animates the provided dialogue string with a typewriter effect
    private void animateDialogue(String fullText) {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop(); // Stop any previous typing animations
        }
        dialogueArea.setText(""); // Clear existing text
        fullTextToAnimate = fullText;
        typewriterIndex = 0;

        // Create a timer that adds one character at a time
        typewriterTimer = new Timer(40, e -> {
            if (typewriterIndex < fullTextToAnimate.length()) {
                dialogueArea.append(String.valueOf(fullTextToAnimate.charAt(typewriterIndex)));
                typewriterIndex++;
                dialogueArea.setCaretPosition(dialogueArea.getDocument().getLength()); // Auto-scroll
            } else {
                typewriterTimer.stop(); // Stop once all characters are typed
            }
        });
        typewriterTimer.start(); // Begin the animation
    }

    // Initializes all UI components and adds them to the JFrame
    private void setupUI() {
        // Player health label and bar
        JLabel playerHealthText = new JLabel("Player Health");
        playerHealthText.setBounds(50, 20, 120, 20);
        add(playerHealthText);

        playerHealthBar = new JProgressBar(0, 100);
        playerHealthBar.setValue(playerHealth);
        playerHealthBar.setForeground(Color.GREEN);
        playerHealthBar.setBounds(170, 20, 150, 20);
        add(playerHealthBar);

        // Player sanity label and bar
        JLabel sanityText = new JLabel("Player Sanity");
        sanityText.setBounds(50, 50, 120, 20);
        add(sanityText);

        playerSanityBar = new JProgressBar(0, 100);
        playerSanityBar.setValue(playerSanity);
        playerSanityBar.setForeground(Color.BLUE);
        playerSanityBar.setBounds(170, 50, 150, 20);
        add(playerSanityBar);

        // Panel to draw player and boss visuals
        DrawPanel drawPanel = new DrawPanel();
        drawPanel.setBounds(50, 100, 350, 170);
        add(drawPanel);

        // Boss health label and bar
        JLabel bossHealthText = new JLabel("Boss Health");
        bossHealthText.setBounds(250, 270, 120, 20);
        add(bossHealthText);

        bossHealthBar = new JProgressBar(0, 100);
        bossHealthBar.setValue(bossHealth);
        bossHealthBar.setForeground(Color.RED);
        bossHealthBar.setBounds(370, 270, 150, 20);
        add(bossHealthBar);

        // Dialogue area inside a scroll pane
        dialogueArea = new JTextArea();
        dialogueArea.setEditable(false);
        dialogueArea.setLineWrap(true);
        dialogueArea.setWrapStyleWord(true);
        dialogueArea.setFont(new Font("Serif", Font.PLAIN, 16));
        dialogueArea.setBackground(Color.WHITE);

        dialogueScrollPane = new JScrollPane(dialogueArea);
        dialogueScrollPane.setBounds(50, 310, 500, 70);
        add(dialogueScrollPane);

        // Attack button
        attackButton = new JButton("ATTACK");
        attackButton.setBounds(580, 150, 120, 40);
        attackButton.addActionListener(this::handleAttack);
        add(attackButton);

        // Skill button
        skillButton = new JButton("SKILL");
        skillButton.setBounds(580, 200, 120, 40);
        skillButton.addActionListener(this::handleSkill);
        add(skillButton);

        // Defend button
        defendButton = new JButton("DEFEND");
        defendButton.setBounds(580, 250, 120, 40);
        defendButton.addActionListener(this::handleDefend);
        add(defendButton);
    }

    // Action performed when the player chooses to attack
    private void handleAttack(ActionEvent e) {
        bossHealth -= 15; // Deal 15 damage to boss
        if (bossHealth < 0) bossHealth = 0;
        bossHealthBar.setValue(bossHealth); // Update boss health bar
        setDialogue("You attacked the boss! -15 HP"); // Show feedback
        checkBattleProgress(); // Check win/loss
        simulateEnemyTurn(); // Let the boss strike back
        repaint(); // Redraw UI
    }

    // Action performed when the player uses a skill
    private void handleSkill(ActionEvent e) {
        bossHealth -= 25; // Skill deals more damage
        playerHealth -= 10; // But costs some player health
        if (bossHealth < 0) bossHealth = 0;
        if (playerHealth < 0) playerHealth = 0;
        bossHealthBar.setValue(bossHealth);
        playerHealthBar.setValue(playerHealth);
        setDialogue("You used SKILL! -25 HP to boss, -10 HP to you");
        checkBattleProgress();
        simulateEnemyTurn();
        repaint();
    }

    // Action performed when the player chooses to defend
    private void handleDefend(ActionEvent e) {
        defending = true; // Flag set to reduce damage on enemy turn
        setDialogue("You prepare to defend.");
        simulateEnemyTurn();
        repaint();
    }

    // Simulates the boss's turn after the player acts
    private void simulateEnemyTurn() {
        int damage = defending ? 5 : 15; // Reduced damage if defending
        playerHealth -= damage;
        if (playerHealth < 0) playerHealth = 0;
        playerHealthBar.setValue(playerHealth); // Update health
        defending = false; // Reset defend state
        setDialogue("Boss strikes back! You take " + damage + " damage.");
        checkBattleProgress();
    }

    // Checks battle outcome after every turn
    private void checkBattleProgress() {
        if (playerHealth <= 0) {
            setDialogue("You were defeated... The battle ends.");
            disableButtons(); // Disable further actions
        } else if (bossHealth <= 0) {
            disableButtons(); // Disable actions
            showVictoryPhaseTransition(); // Move to next phase
        }
    }

    // Displays victory dialogue and transitions to boss phase 2
    private void showVictoryPhaseTransition() {
        String victoryText = "You defeated the boss!\n\nRigurd: WE WILL PREVAIL!\nCaroline: What a battle...\n\nEveryone celebrates the victory, unaware of the danger to come...";
        animateDialogue(victoryText);

        // Transition to Phase 2 after delay
        Timer timer = new Timer(9000, e -> {
            dispose(); // Close this window
            new Zone6BossPhase2(); // Launch the next phase
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Sets the dialogue to be animated
    private void setDialogue(String text) {
        animateDialogue(text);
    }

    // Disables all battle buttons
    private void disableButtons() {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        defendButton.setEnabled(false);
    }

    // Custom panel that draws the player and boss boxes
    private class DrawPanel extends JPanel {
        public DrawPanel() {
            setOpaque(false); // Transparent background
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw player box
            g.setColor(Color.CYAN);
            g.fillRect(0, 0, 150, 150);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 150, 150);
            g.drawString("PLAYER", 50, 75);

            // Draw boss box
            g.setColor(Color.PINK);
            g.fillRect(200, 0, 150, 150);
            g.setColor(Color.BLACK);
            g.drawRect(200, 0, 150, 150);
            g.drawString("BOSS", 270, 75);
        }
    }

    // Main method to launch the boss phase
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Zone6BossPhase1::new);
    }
}
