import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

public class Zone6BossPhase2 extends JFrame {

    private BackgroundPanel backgroundPanel;

    // UI elements for health/sanity and buttons
    private JProgressBar playerHealthBar, playerSanityBar, bossHealthBar;
    private JTextArea dialogueTextArea;
    private JScrollPane dialogueScrollPane;
    private JButton judgementButton, realityBendingButton;
    private JButton quitButton;

    // Game state variables
    private int playerHealth = 100;
    private int playerSanity = 100;
    private int bossHealth = 200;

    private boolean bossDestructionUsed = false;
    private boolean playerTurn = false;

    // Typewriter effect components
    private Timer dialogueTimer;
    private String fullDialogueText = "";
    private int dialogueIndex = 0;

    // Constructor
    public Zone6BossPhase2() {
        setTitle("Zone 6 - Boss Phase 2");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up background panel with zone image
        backgroundPanel = new BackgroundPanel("/zone6_battle_background.jpg");
        setContentPane(backgroundPanel);

        setupUI();                // Initialize UI elements
        showPreBattleDialogue(); // Start phase with pre-battle cutscene

        setVisible(true);
    }

    // Method to build all UI components and place them on the screen
    private void setupUI() {
        // Player health label
        JLabel playerHealthText = new JLabel("Player Health");
        playerHealthText.setForeground(Color.WHITE);
        playerHealthText.setBounds(20, 20, 120, 20);
        backgroundPanel.add(playerHealthText);

        // Quit button (shown only after final win/loss)
        quitButton = new JButton("QUIT");
        quitButton.setBounds(400, 600, 100, 40);
        quitButton.setVisible(false);  // Hide until battle ends
        quitButton.addActionListener(e -> System.exit(0));
        backgroundPanel.add(quitButton);

        // Player health bar
        playerHealthBar = new JProgressBar(0, 100);
        playerHealthBar.setValue(playerHealth);
        playerHealthBar.setForeground(Color.GREEN);
        playerHealthBar.setBounds(140, 20, 150, 20);
        backgroundPanel.add(playerHealthBar);

        // Player sanity label
        JLabel playerSanityText = new JLabel("Player Sanity");
        playerSanityText.setForeground(Color.WHITE);
        playerSanityText.setBounds(20, 50, 120, 20);
        backgroundPanel.add(playerSanityText);

        // Player sanity bar
        playerSanityBar = new JProgressBar(0, 100);
        playerSanityBar.setValue(playerSanity);
        playerSanityBar.setForeground(Color.CYAN);
        playerSanityBar.setBounds(140, 50, 150, 20);
        backgroundPanel.add(playerSanityBar);

        // Boss health label
        JLabel bossHealthText = new JLabel("Boss Health");
        bossHealthText.setForeground(Color.WHITE);
        bossHealthText.setBounds(620, 20, 120, 20);
        backgroundPanel.add(bossHealthText);

        // Boss health bar
        bossHealthBar = new JProgressBar(0, 200);
        bossHealthBar.setValue(bossHealth);
        bossHealthBar.setForeground(Color.RED);
        bossHealthBar.setBounds(740, 20, 140, 20);
        backgroundPanel.add(bossHealthBar);

        // Dialogue text area inside scroll pane
        dialogueTextArea = new JTextArea();
        dialogueTextArea.setLineWrap(true);
        dialogueTextArea.setWrapStyleWord(true);
        dialogueTextArea.setEditable(false);
        dialogueTextArea.setBackground(new Color(0, 0, 0, 150));
        dialogueTextArea.setForeground(Color.WHITE);
        dialogueTextArea.setFont(new Font("Serif", Font.PLAIN, 16));
        dialogueTextArea.setMargin(new Insets(10, 10, 10, 10));

        // Scroll pane for dialogue area
        dialogueScrollPane = new JScrollPane(dialogueTextArea);
        dialogueScrollPane.setBounds(50, 430, 780, 150);
        backgroundPanel.add(dialogueScrollPane);

        // Judgement attack button
        judgementButton = new JButton("Judgement");
        judgementButton.setBounds(100, 360, 140, 40);
        judgementButton.addActionListener(this::handleJudgement);
        judgementButton.setEnabled(false); // Enabled after dialogue
        backgroundPanel.add(judgementButton);

        // Reality Bending skill button
        realityBendingButton = new JButton("Reality Bending");
        realityBendingButton.setBounds(260, 360, 160, 40);
        realityBendingButton.addActionListener(this::handleRealityBending);
        realityBendingButton.setEnabled(false);
        backgroundPanel.add(realityBendingButton);

        // Player box (with halo) to represent the angel
        JPanel playerBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.drawRect(0, 0, getWidth()-1, getHeight()-1);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = "PLAYER (Angel)";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                g.drawString(text, (getWidth()-textWidth)/2, (getHeight()+textHeight)/2 - 5);

                // Draw angel halo
                g.setColor(Color.YELLOW);
                g.drawOval(getWidth()/2 - 20, 5, 40, 15);
            }
        };
        playerBox.setOpaque(false);
        playerBox.setBounds(50, 150, 200, 100);
        backgroundPanel.add(playerBox);

        // Boss box (with horns) to represent the devil
        JPanel bossBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.drawRect(0, 0, getWidth()-1, getHeight()-1);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = "BOSS (Devil)";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                g.drawString(text, (getWidth()-textWidth)/2, (getHeight()+textHeight)/2 - 5);

                // Draw devil horns
                g.setColor(Color.RED);
                int hornWidth = 20, hornHeight = 30;
                g.fillPolygon(new int[]{0, hornWidth, 0}, new int[]{0, hornHeight, hornHeight/2}, 3); // left horn
                g.fillPolygon(new int[]{getWidth(), getWidth()-hornWidth, getWidth()}, new int[]{0, hornHeight, hornHeight/2}, 3); // right horn
            }
        };
        bossBox.setOpaque(false);
        bossBox.setBounds(650, 150, 200, 100);
        backgroundPanel.add(bossBox);
    }

    // Show dramatic story sequence before the battle begins
    private void showPreBattleDialogue() {
        String preBattleText = """
                This should be the end, but what is this sense of unease in the atmosphere.
                You: EVERYONE GET DOWN NOW.
                The defeated “boss” looks to be evolving in a way that we cannot comprehend. It keeps growing larger, but worst of all, it came back and started with a move nobody has seen before.
                Caroline: I ca-
                Caroline was struck with an attack so fast that nobody even reacted.
                Rigurd: DON’T FALTER, KEEP FORMATION.
                You wanted to listen but Caroline was your line of support and healing, without her help that would be impossible.
                You: NO, THAT WILL ONLY KILL US FASTER.
                But it was already too late, Rigurd was already flying past your face.
                Darius: We need to retreat NOW.
                The only path back was blocked by the newfound horror that has been ripping through your team. When Darius looks back, he stopped mid yell…
                The only people left were you and ???.
                You: Just live please, whatever you do.
                ???: I never really thought it would happen like this. I was too late to bloom.
                You: What are you talking about?
                Right before she said anything else, the monster grabs her.
                ???: I am the GODDESS OF END. This body can hold no longer, so now I entrust you, “player name”.

                All of a sudden, a rush of power runs through you. Everything becomes a blur for a second, but then you gained consciousness and had a newfound power.
                """;

        // Start typewriter effect, then enable buttons and music
        startTypewriterEffect(preBattleText, () -> {
            judgementButton.setEnabled(true);
            realityBendingButton.setEnabled(true);
            playerTurn = true;
            playSound("/zone6_music.wav");
        });
    }

    // Typewriter animation for story/dialogue text
    private void startTypewriterEffect(String text, Runnable afterComplete) {
        fullDialogueText = text;
        dialogueIndex = 0;
        dialogueTextArea.setText("");

        dialogueTimer = new Timer(30, e -> {
            if (dialogueIndex < fullDialogueText.length()) {
                dialogueTextArea.append(String.valueOf(fullDialogueText.charAt(dialogueIndex)));
                dialogueIndex++;
                dialogueTextArea.setCaretPosition(dialogueTextArea.getDocument().getLength()); // auto-scroll
            } else {
                dialogueTimer.stop();
                if (afterComplete != null) {
                    afterComplete.run();
                }
            }
        });

        dialogueTimer.start();
    }

    // Handles Judgement attack logic
    private void handleJudgement(ActionEvent e) {
        if (!playerTurn) return;

        int damage = 30;
        bossHealth -= damage;
        bossHealthBar.setValue(bossHealth);
        appendDialogue("You used Judgement! -30 HP to boss.");
        playerTurn = false;

        // Check if boss defeated
        if (bossHealth <= 0) {
            endBattle(true);
            return;
        }

        // Boss's turn if still alive
        bossTurn();
    }

    // Handles Reality Bending skill logic
    private void handleRealityBending(ActionEvent e) {
        if (!playerTurn) return;

        int sanityCost = 20;
        playerSanity -= sanityCost;
        if (playerSanity < 0) playerSanity = 0;
        playerSanityBar.setValue(playerSanity);

        boolean stunSuccess = Math.random() < 0.5; // 50% chance to stun

        if (stunSuccess) {
            appendDialogue("You used Reality Bending! Boss is stunned this turn.");
        } else {
            appendDialogue("You used Reality Bending! But the boss resisted.");
        }

        playerTurn = false;

        if (!stunSuccess) {
            // Boss attacks immediately
            bossTurn();
        } else {
            // Wait one turn, skip boss action
            Timer delay = new Timer(1500, evt -> {
                playerTurn = true;
                appendDialogue("Your turn again.");
            });
            delay.setRepeats(false);
            delay.start();
        }
    }


 // Handles the boss's turn during battle
    private void bossTurn() {
        // Check if the boss should use its special "Destruction" move (only once, and if below half HP)
        if (bossHealth <= 100 && !bossDestructionUsed) {
            if (playerHealth > 0) {
                bossDestructionUsed = true; // Ensure this move can only happen once
                int damage = playerHealth / 2; // Destruction does 50% of current HP
                playerHealth -= damage;
                if (playerHealth < 0) playerHealth = 0; // Clamp at 0
                playerHealthBar.setValue(playerHealth);
                appendDialogue("Boss uses DESTRUCTION! You lose 50% of your HP.");
                checkPlayerDefeat(); // Check if player died

                // Allow player to take next turn
                judgementButton.setEnabled(true);
                realityBendingButton.setEnabled(true);
                playerTurn = true;
                return;
            }
        }

        // If Destruction wasn't triggered, boss performs normal attack
        int damage = 20;
        playerHealth -= damage;
        if (playerHealth < 0) playerHealth = 0; // Clamp at 0
        playerHealthBar.setValue(playerHealth);
        appendDialogue("Boss attacks! You take " + damage + " damage.");
        checkPlayerDefeat(); // Check if player died

        // If player survived, give turn back to player
        playerTurn = true;
        appendDialogue("Your turn.");
    }

    // Adds dialogue text to the text area and scrolls to the bottom
    private void appendDialogue(String text) {
        dialogueTextArea.append("\n" + text);
        dialogueTextArea.setCaretPosition(dialogueTextArea.getDocument().getLength()); // auto-scroll
    }

    // Checks if the player has lost (HP reached 0 or below)
    private void checkPlayerDefeat() {
        if (playerHealth <= 0) {
            endBattle(false); // false = player lost
        }
    }

    // Ends the battle and triggers the final narrative
    private void endBattle(boolean playerWon) {
        // Disable action buttons so no further input can be made
        judgementButton.setEnabled(false);
        realityBendingButton.setEnabled(false);

        // Choose appropriate ending narrative
        String endingText;
        if (playerWon) {
            endingText = """
                    Given only one option, after winning the battle, you must live with all the built up guilt, along with everything the Goddess of End left behind.
                    Lost in a void of regret, all you can do is wander endlessly hoping that you have done the right thing.
                    Now back into the real world you finally realize.
                    “Victory did not come without a price… The price of your own mind becoming the very image of an enemy.”
                    """;
        } else {
            endingText = """
                    The world fades from your vision slowly… Having no idea whether you have won or lost, you still end up in a field of flowers. It seems rather peaceful here, after finally making it out.
                    You would think that after beating the trials, at least you would be sent home…
                    Perhaps you didn’t make it.
                    As long as there is peace, you feel as if you have won in some way.
                    “I suppose this is how it was always meant to be… Peace, finally away from the neverending despair.”
                    """;
        }

        // Clear the dialogue area and display ending text using typewriter effect
        dialogueTextArea.setText("");
        startTypewriterEffect(endingText, () -> {
            // After ending dialogue is typed out, add a final message and show the QUIT button
            appendDialogue("\n--- End of Zone 6 Boss Battle ---");
            quitButton.setVisible(true); // Let player close the game
        });
    }

    // Plays the specified audio file in a loop (background music)
    private void playSound(String soundFileName) {
        new Thread(() -> {
            try {
                URL url = getClass().getResource(soundFileName);
                if (url == null) {
                    System.out.println("Sound file not found: " + soundFileName);
                    return;
                }

                // Load audio clip and play
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop music
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start(); // Run audio loading/playing in its own thread
    }

    // Launches the Zone 6 Boss Phase 2 screen
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Zone6BossPhase2::new); // Safe GUI thread start
    }
}
