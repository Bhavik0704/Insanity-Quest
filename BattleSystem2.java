import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BattleSystem extends JFrame {

    // Progress bars to show player's health and sanity visually
    private JProgressBar healthBar, sanityBar;

    // Labels to represent player and opponent in the UI
    private JLabel playerLabel, opponentLabel;

    // Label to display battle dialogue/messages to the player
    private JLabel dialogueLabel;

    // Buttons for player actions in battle: Attack, Skill, and Defend
    private JButton attackButton, skillButton, defendButton;

    // Player and opponent stats
    private int playerHealth = 100;      // Player's current health
    private int playerSanity = 100;      // Player's current sanity
    private int opponentHealth = 100;    // Opponent's current health

    // Base damage and defense values used in calculations
    private int baseAttack = 15;
    private int baseDefense = 5;

    // Flag to track if player is defending (reduces damage taken next turn)
    private boolean defending = false;

    // Callback to run after battle ends (for returning to main game)
    private Runnable onBattleEnd;

    // Default constructor, calls main constructor with no callback
    public BattleSystem() {
        this(null);
    }

    // Main constructor accepts a Runnable callback to execute after battle ends
    public BattleSystem(Runnable onBattleEnd) {
        this.onBattleEnd = onBattleEnd;

        // Set up window title, size, close operation, and layout
        setTitle("Battle Screen");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only dispose this window on close
        setLayout(null); // Use absolute positioning for custom UI layout

        setupUI();  // Build UI components and add them to frame

        setVisible(true); // Show the battle window
    }

    // Setup UI components and add to the frame
    private void setupUI() {
        // Label for "HEALTH" text
        JLabel healthText = new JLabel("HEALTH");
        healthText.setBounds(50, 20, 60, 20);  // Position and size
        add(healthText);

        // Health bar showing player's current health (green)
        healthBar = new JProgressBar(0, 100);
        healthBar.setValue(playerHealth);
        healthBar.setForeground(Color.GREEN);
        healthBar.setBounds(120, 20, 150, 20);
        add(healthBar);

        // Label for "SANITY" text
        JLabel sanityText = new JLabel("SANITY");
        sanityText.setBounds(50, 50, 60, 20);
        add(sanityText);

        // Sanity bar showing player's current sanity (red)
        sanityBar = new JProgressBar(0, 100);
        sanityBar.setValue(playerSanity);
        sanityBar.setForeground(Color.RED);
        sanityBar.setBounds(120, 50, 150, 20);
        add(sanityBar);

        // Label to represent player sprite or name
        playerLabel = new JLabel("PLAYER", SwingConstants.CENTER);
        playerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerLabel.setBounds(70, 100, 120, 160);
        add(playerLabel);

        // Label to represent opponent sprite or name
        opponentLabel = new JLabel("OPPONENT", SwingConstants.CENTER);
        opponentLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        opponentLabel.setBounds(600, 100, 120, 160);
        add(opponentLabel);

        // Dialogue label to show battle messages and updates
        dialogueLabel = new JLabel("Battle Start!", SwingConstants.CENTER);
        dialogueLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dialogueLabel.setOpaque(true);
        dialogueLabel.setBackground(Color.WHITE);
        dialogueLabel.setBounds(50, 400, 700, 60);
        add(dialogueLabel);

        // Attack button, triggers attack action when clicked
        attackButton = new JButton("ATTACK");
        attackButton.setBounds(600, 300, 120, 40);
        attackButton.addActionListener(this::handleAttack);
        add(attackButton);

        // Skill button, triggers skill action (sanity-based attack)
        skillButton = new JButton("SKILL");
        skillButton.setBounds(600, 350, 120, 40);
        skillButton.addActionListener(this::handleSkill);
        add(skillButton);

        // Defend button, triggers defend action (reduce incoming damage)
        defendButton = new JButton("DEFEND");
        defendButton.setBounds(600, 400, 120, 40);
        defendButton.addActionListener(this::handleDefend);
        add(defendButton);
    }

    // Handle when player clicks ATTACK button
    private void handleAttack(ActionEvent e) {
        int damage = baseAttack;            // Base damage dealt
        opponentHealth -= damage;           // Reduce opponent's health by damage
        dialogueLabel.setText("You attacked! Dealt " + damage + " damage."); // Update dialogue
        checkBattleEnd();                   // Check if battle ended
        simulateEnemyTurn();                // Let opponent take their turn
    }

    // Handle when player clicks SKILL button (requires sanity)
    private void handleSkill(ActionEvent e) {
        if (playerSanity < 20) {            // Check if sanity is too low to use skill
            dialogueLabel.setText("Too insane to focus and use a skill.");
        } else {
            // Calculate skill damage based on sanity multiplier (double damage scaled by sanity%)
            int damage = (int) (baseAttack * (playerSanity / 100.0) * 2);
            opponentHealth -= damage;       // Reduce opponent health
            playerSanity -= 10;             // Reduce player's sanity as cost
            sanityBar.setValue(playerSanity); // Update sanity bar display
            dialogueLabel.setText("You used a skill! Dealt " + damage + " damage.");
        }
        checkBattleEnd();                   // Check if battle ended
        simulateEnemyTurn();                // Opponent turn
    }

    // Handle when player clicks DEFEND button
    private void handleDefend(ActionEvent e) {
        defending = true;                   // Set defending flag true to reduce damage next turn
        dialogueLabel.setText("You prepare to defend. Damage will be reduced next turn.");
        simulateEnemyTurn();                // Opponent turn
    }

    // Simulate opponent's turn after player action
    private void simulateEnemyTurn() {
        if (opponentHealth <= 0) return;   // If opponent defeated, skip enemy turn

        int enemyDamage = 20;              // Opponent's base damage

        // If player defended last turn, reduce damage by player's base defense
        if (defending) {
            enemyDamage -= baseDefense;
            defending = false;             // Reset defending flag after reducing damage
        }

        playerHealth -= enemyDamage;       // Subtract damage from player's health
        if (playerHealth < 0) playerHealth = 0;  // Clamp health to minimum zero
        healthBar.setValue(playerHealth);  // Update health bar UI

        // Append enemy attack message to dialogue
        dialogueLabel.setText(dialogueLabel.getText() + " | Enemy hits back for " + enemyDamage + "!");
        checkBattleEnd();                  // Check if battle ended after enemy attack
    }

    // Check if the battle has ended due to health reaching zero for player or opponent
    private void checkBattleEnd() {
        if (opponentHealth <= 0) {
            dialogueLabel.setText("You win!");  // Player won
            endBattleAfterDelay();               // End battle with a short delay
        } else if (playerHealth <= 0) {
            dialogueLabel.setText("You were defeated..."); // Player lost
            endBattleAfterDelay();               // End battle after delay
        }
    }

    // End the battle after 2 seconds, close window and run callback if provided
    private void endBattleAfterDelay() {
        Timer timer = new Timer(2000, e -> {
            dispose();                         // Close battle window
            if (onBattleEnd != null) {
                onBattleEnd.run();             // Call callback to return to main game
            }
        });
        timer.setRepeats(false);                // Run only once
        timer.start();                         // Start the timer
    }
