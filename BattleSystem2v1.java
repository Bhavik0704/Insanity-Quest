import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class BattleSystem2v1 extends JFrame {

    // Labels representing the sprites of player, ally (Frog), and miniboss
    private JLabel playerSprite, frogSprite, minibossSprite;

    // Progress bars showing health for player, Frog, miniboss and sanity for player
    private JProgressBar playerHealthBar, frogHealthBar, minibossHealthBar, playerSanityBar;
    // Text area for showing battle dialogue and narration
    private JTextArea dialogueArea;
    // Scroll pane to hold the dialogue area (in case text overflows)
    private JScrollPane dialogueScroll;
    // Buttons for player's actions
    private JButton attackButton, skillButton, defendButton;

    // Current stats for health and sanity
    private int playerHealth = 100;
    private int frogHealth = 130;
    private int minibossHealth = 200;
    private int playerSanity = 100;

    // Base attack values for each character for damage calculations
    private final int playerBaseAttack = 20;
    private final int frogBaseAttack = 30;
    private final int minibossBaseAttack = 35;

    // Flags to track defending, protection, and stun statuses
    private boolean playerDefending = false;
    private boolean frogDefending = false;
    private boolean frogProtected = false;
    private boolean minibossStunned = false;

    // Callback to run when battle ends (used to return control to game)
    private Runnable onBattleEnd;
    // Random number generator for AI decisions and chance events
    private Random random = new Random();

    // Constructor: sets up the battle window and initializes UI and intro dialogue
    public BattleSystem2v1(Runnable onBattleEnd) {
        this.onBattleEnd = onBattleEnd;

        setTitle("Battle: Player + Frog vs Miniboss"); // Window title
        setSize(850, 650); // Set window size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose window when closed
        setLayout(null); // Use absolute positioning to precisely place UI elements

        setupUI(); // Build all UI components
        showIntroDialogue(); // Show story intro before battle starts

        setVisible(true); // Make window visible to user
    }

    // Method to create and position all UI components
    private void setupUI() {
        // Label for Player Health bar
        JLabel playerHealthText = new JLabel("PLAYER HP");
        playerHealthText.setBounds(30, 20, 100, 20);
        add(playerHealthText);

        // Player sprite label (colored rectangle with "PLAYER" text)
        playerSprite = new JLabel("PLAYER", SwingConstants.CENTER);
        playerSprite.setOpaque(true);
        playerSprite.setBackground(Color.GREEN);
        playerSprite.setBounds(100, 100, 60, 80);
        add(playerSprite);

        // Frog sprite label (ally NPC)
        frogSprite = new JLabel("FROG", SwingConstants.CENTER);
        frogSprite.setOpaque(true);
        frogSprite.setBackground(Color.CYAN);
        frogSprite.setBounds(200, 200, 60, 80);
        add(frogSprite);

        // Miniboss sprite label (enemy)
        minibossSprite = new JLabel("MINIBOSS", SwingConstants.CENTER);
        minibossSprite.setOpaque(true);
        minibossSprite.setBackground(Color.RED);
        minibossSprite.setBounds(600, 120, 80, 100);
        add(minibossSprite);

        // Player health progress bar setup
        playerHealthBar = new JProgressBar(0, 100);
        playerHealthBar.setValue(playerHealth);
        playerHealthBar.setForeground(Color.GREEN);
        playerHealthBar.setBounds(130, 20, 150, 20);
        add(playerHealthBar);

        // Player sanity label and progress bar (sanity is a key mechanic)
        JLabel sanityLabel = new JLabel("SANITY");
        sanityLabel.setBounds(30, 45, 100, 20);
        add(sanityLabel);

        playerSanityBar = new JProgressBar(0, 100);
        playerSanityBar.setValue(playerSanity);
        playerSanityBar.setForeground(Color.MAGENTA);
        playerSanityBar.setBounds(130, 45, 150, 20);
        add(playerSanityBar);

        // Frog health label and progress bar (shows ally's HP)
        JLabel frogHealthText = new JLabel("FROG HP");
        frogHealthText.setBounds(30, 280, 100, 20);
        add(frogHealthText);

        frogHealthBar = new JProgressBar(0, 130);
        frogHealthBar.setValue(frogHealth);
        frogHealthBar.setForeground(Color.BLUE);
        frogHealthBar.setBounds(130, 280, 150, 20);
        add(frogHealthBar);

        // Miniboss health label and progress bar (shows enemy HP)
        JLabel minibossLabel = new JLabel("MINIBOSS");
        minibossLabel.setBounds(600, 20, 100, 20);
        add(minibossLabel);

        minibossHealthBar = new JProgressBar(0, 200);
        minibossHealthBar.setValue(minibossHealth);
        minibossHealthBar.setForeground(Color.RED);
        minibossHealthBar.setBounds(700, 20, 30, 20);
        minibossHealthBar.setStringPainted(false); // Hide percentage text for style
        add(minibossHealthBar);

        // Dialogue area to show ongoing battle narration and feedback
        dialogueArea = new JTextArea();
        dialogueArea.setEditable(false);
        dialogueArea.setLineWrap(true); // Wrap text within the box
        dialogueArea.setWrapStyleWord(true); // Wrap at word boundaries
        dialogueArea.setFont(new Font("Arial", Font.PLAIN, 14));
        dialogueScroll = new JScrollPane(dialogueArea); // Scroll bar in case of overflow
        dialogueScroll.setBounds(30, 500, 770, 100);
        add(dialogueScroll);

        // Player action buttons setup with positions and listeners
        attackButton = new JButton("ATTACK");
        attackButton.setBounds(650, 300, 120, 40);
        attackButton.addActionListener(this::handleAttack);
        add(attackButton);

        skillButton = new JButton("SKILL");
        skillButton.setBounds(650, 350, 120, 40);
        skillButton.addActionListener(this::handleSkill);
        add(skillButton);

        defendButton = new JButton("DEFEND");
        defendButton.setBounds(650, 400, 120, 40);
        defendButton.addActionListener(this::handleDefend);
        add(defendButton);
    }

    // Display the introductory story dialogue before battle starts
    private void showIntroDialogue() {
        String intro = """
                After following Frog on your journey, the atmosphere changes significantly.
                Something extremely dangerous is crossing your path.
                You decide it would be best to avoid fighting.
                The people here are talking, not full sentences, but intelligent.

                Frog (frightened): "Do not move…"
                You: "Wh-"
                Frog quickly covers your mouth.
                An evolved human, tall and strong, walks past but suddenly turns around.
                Frog: "It's live or die now, let's hope we can both survive this."

                Battle starts!
                """;
        typeDialogue(intro); // Use typewriter effect to display text gradually
    }

    // Handler for when the player clicks the attack button
    private void handleAttack(ActionEvent e) {
        int damage = playerBaseAttack; // Basic damage without modifiers
        minibossHealth -= damage; // Reduce enemy health
        if (minibossHealth < 0) minibossHealth = 0; // Prevent negative health

        playerSanity -= 5; // Using attack reduces player sanity slightly
        updateBars(); // Refresh all health and sanity bars on UI

        typeDialogue("Player attacks Miniboss for " + damage + " damage.");

        // After attack animation/dialogue, Frog takes turn with small delay
        new Timer(1200, evt -> {
            ((Timer) evt.getSource()).stop();
            frogTurn();
        }).start();
    }

    // Handler for player using a skill (stronger attack, costs more sanity)
    private void handleSkill(ActionEvent e) {
        int damage = playerBaseAttack + 15; // Skill attack stronger than normal attack
        minibossHealth -= damage;
        if (minibossHealth < 0) minibossHealth = 0;

        playerSanity -= 15; // Skills cost more sanity
        updateBars();

        typeDialogue("Player uses Skill on Miniboss for " + damage + " damage!");

        // Frog takes turn after player skill, delay for effect
        new Timer(1200, evt -> {
            ((Timer) evt.getSource()).stop();
            frogTurn();
        }).start();
    }

    // Handler for player defending (reduce damage next turn)
    private void handleDefend(ActionEvent e) {
        playerDefending = true; // Flag used to halve damage taken
        typeDialogue("Player prepares to defend. Incoming damage will be halved.");

        // Frog's turn after defending, with a delay
        new Timer(1000, evt -> {
            ((Timer) evt.getSource()).stop();
            frogTurn();
        }).start();
    }

    // Frog's AI turn: randomly chooses between attack, stun, or protect
    private void frogTurn() {
        if (frogHealth <= 0) {
            // If Frog is down, skip turn and let miniboss act
            typeDialogue("Frog is down and cannot act.");
            new Timer(1000, evt -> {
                ((Timer) evt.getSource()).stop();
                minibossTurn();
            }).start();
            return;
        }

        // Randomly decide Frog's action
        int choice = random.nextInt(3);
        switch (choice) {
            case 0 -> {
                // Attack miniboss
                minibossHealth -= frogBaseAttack;
                if (minibossHealth < 0) minibossHealth = 0;
                typeDialogue("Frog attacks Miniboss for " + frogBaseAttack + " damage.");
            }
            case 1 -> {
                // Use stun to skip miniboss's next turn
                minibossStunned = true;
                typeDialogue("Frog uses Stun! Miniboss is stunned.");
            }
            case 2 -> {
                // Use full protect to block miniboss's next attack
                frogProtected = true;
                typeDialogue("Frog uses Full Protect! He will block the next hit.");
            }
        }

        updateBars();

        // After Frog acts, miniboss takes turn with delay
        new Timer(1300, evt -> {
            ((Timer) evt.getSource()).stop();
            minibossTurn();
        }).start();
    }

    // Miniboss AI turn: attack player or Frog unless stunned
    private void minibossTurn() {
        if (minibossHealth <= 0) {
            // Miniboss defeated, show victory sequence
            showVictorySequence();
            return;
        }

        if (minibossStunned) {
            // Miniboss skips turn if stunned, reset stun flag
            typeDialogue("Miniboss is stunned and misses its turn.");
            minibossStunned = false;
            return;
        }

        // Randomly choose to attack Frog or player (if Frog alive)
        boolean targetFrog = frogHealth > 0 && random.nextBoolean();
        int damage = minibossBaseAttack;

        if (targetFrog) {
            if (frogProtected) {
                // Frog blocks entire attack using protection skill
                typeDialogue("Miniboss attacks, but Frog blocks it completely!");
                frogProtected = false; // Protection used up
            } else {
                // If Frog is defending, damage is halved
                if (frogDefending) {
                    damage /= 2;
                    frogDefending = false; // Reset defend status
                }
                frogHealth -= damage;
                if (frogHealth < 0) frogHealth = 0;
                typeDialogue("Miniboss attacks Frog for " + damage + " damage.");
            }
        } else {
            // Miniboss attacks player
            if (playerDefending) {
                damage /= 2; // Damage halved if player defended
                playerDefending = false; // Reset defending flag
            }
            playerHealth -= damage;
            if (playerHealth < 0) playerHealth = 0;
            typeDialogue("Miniboss attacks Player for " + damage + " damage.");
        }

        updateBars();

        // After miniboss attacks, check for battle end condition with delay
        new Timer(1400, evt -> {
            ((Timer) evt.getSource()).stop();
            checkBattleEnd();
        }).start();
    }

    // Check if the battle is over due to defeat or victory
    private void checkBattleEnd() {
        if (minibossHealth <= 0) {
            // Player & Frog win, show victory narrative and end battle
            showVictorySequence();
        } else if (playerHealth <= 0 && frogHealth <= 0) {
            // Both player and Frog defeated, battle lost
            typeDialogue("You and Frog have been defeated...");
            endBattle(false);
        }
    }

    // Victory sequence narrative and ending battle cleanup
    private void showVictorySequence() {
        // Disable player buttons to prevent further input
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        defendButton.setEnabled(false);

        String finalWords = """
                You actually managed to defeat this beast with Frog's help.

                Frog seems enlightened but suddenly is attacked by something piercing his chest.
                It came from the defeated evolved human’s last stand.

                Frog (weakly): "Just go, it'll take you where you should be…"

                You obtained a Stained Map!
                After laying Frog to rest, you decide to move forward.
                That area was only 40-49% insane. How much harder will it get?
                """;

        typeDialogue(finalWords);

        // Close battle window and trigger onBattleEnd callback after a delay to allow reading
        Timer timer = new Timer(9000, e -> {
            dispose();
            if (onBattleEnd != null) {
                onBattleEnd.run();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Ends the battle forcibly, disables buttons and closes window after delay
    private void endBattle(boolean won) {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        defendButton.setEnabled(false);

        Timer endTimer = new Timer(2000, e -> {
            dispose();
            if (onBattleEnd != null && won) {
                onBattleEnd.run();
            }
        });
        endTimer.setRepeats(false);
        endTimer.start();
    }

    // Updates all health and sanity bars to reflect current stats
    private void updateBars() {
        playerHealthBar.setValue(playerHealth);
        frogHealthBar.setValue(frogHealth);
        minibossHealthBar.setValue(minibossHealth);
        playerSanityBar.setValue(Math.max(playerSanity, 0)); // Sanity can’t go below 0
    }

    // Typewriter effect for dialogue: disables buttons while text prints and re-enables after
    private void typeDialogue(String message) {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        defendButton.setEnabled(false);

        dialogueArea.setText(""); // Clear previous dialogue
        final char[] chars = message.toCharArray();
        final int[] index = {0}; // Use array to allow modification inside Timer

        // Timer appends one character at a time for typewriter effect
        Timer timer = new Timer(20, e -> {
            if (index[0] < chars.length) {
                dialogueArea.append(String.valueOf(chars[index[0]++]));
                dialogueArea.setCaretPosition(dialogueArea.getDocument().getLength()); // Scroll to bottom
            } else {
                ((Timer) e.getSource()).stop();
                // Enable buttons again when done typing
                attackButton.setEnabled(true);
                skillButton.setEnabled(true);
                defendButton.setEnabled(true);
            }
        });
        timer.start();
    }
