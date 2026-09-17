import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // Screen settings
    final int tileSize = 48; // Size of each tile/sprite in pixels
    final int screenWidth = 768; // Width of the game screen in pixels
    final int screenHeight = 576; // Height of the game screen in pixels

    // Game thread responsible for running the game loop
    Thread gameThread;

    // Game state enumeration to track whether player is exploring or battling
    enum GameState { EXPLORE, BATTLE }
    GameState currentState = GameState.EXPLORE; // Start in exploration mode

    // Player object representing the player character
    Player player = new Player();

    // Current enemy encountered during battle
    Enemy currentEnemy;

    // Font used for drawing dialogue and UI text
    Font dialogueFont = new Font("Arial", Font.PLAIN, 20);

    // Constructor for the game panel
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // Set panel size
        this.setBackground(Color.black); // Set background color
        this.setDoubleBuffered(true); // Enable double buffering for smooth rendering
        this.addKeyListener(this); // Listen for keyboard input
        this.setFocusable(true); // Make sure the panel can receive focus to get input
    }

    // Starts the game loop thread
    public void startGameThread() {
        gameThread = new Thread(this); // Create new thread with this Runnable
        gameThread.start(); // Start the thread, triggering run()
    }

    // Main game loop
    @Override
    public void run() {
        while (gameThread != null) {
            update();   // Update game logic
            repaint();  // Redraw screen
            try {
                Thread.sleep(16); // Pause ~16ms for ~60 frames per second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Update game state
    public void update() {
        if (currentState == GameState.EXPLORE) {
            player.update(); // Update player movement and actions in exploration
            // TODO: Check for enemy encounters or other triggers here
        }
    }

    // Draw the game components depending on the state
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentState == GameState.EXPLORE) {
            player.draw(g); // Draw player in exploration mode
        } else if (currentState == GameState.BATTLE) {
            drawBattleScreen(g); // Draw battle UI
        }
    }

    // Battle Screen and Logic
    
    private boolean playerTurn = true; // Tracks if it's the player's turn in battle
    private boolean battleOver = false; // Flag to determine if battle has ended

    // Starts a battle with the given enemy
    public void startBattle(Enemy enemy) {
        currentEnemy = enemy;    // Set current enemy
        currentState = GameState.BATTLE; // Switch game state to battle
        battleOver = false;      // Reset battle over flag
    }

    // Draws the battle screen UI elements
    private void drawBattleScreen(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, screenWidth, screenHeight); // Background for battle screen

        g.setColor(Color.white);
        g.setFont(dialogueFont);

        // Display player HP and sanity
        g.drawString("Player HP: " + player.hp + "  |  Sanity: " + player.sanity, 50, 50);
        // Display enemy HP
        g.drawString("Enemy HP: " + currentEnemy.hp, 50, 100);

        if (battleOver) {
            g.drawString("Battle Over!", 50, 150); // Show end battle message
            return; // Skip further drawing or actions
        }

        if (playerTurn) {
            // Prompt player for action
            g.drawString("Your Turn - Press A to Attack or S to use Sanity Skill", 50, 200);
        } else {
            g.drawString("Enemy Turn...", 50, 200);
            enemyAction(); // Let enemy perform their turn
        }
    }

    // Logic for enemy's turn
    private void enemyAction() {
        int damage = currentEnemy.attack(); // Enemy attacks, returns damage amount
        player.hp -= damage;                 // Subtract damage from player HP
        player.sanity -= 5;                 // Player sanity decreases each enemy attack

        if (player.hp <= 0) {
            battleOver = true; // Player died, battle ends
        } else {
            playerTurn = true; // Switch turn back to player
        }
    }

    // Player chooses to attack normally
    private void playerAttack() {
        int damage = player.attack();      // Player attack damage
        currentEnemy.hp -= damage;          // Reduce enemy HP
        playerTurn = false;                 // Switch turn to enemy

        if (currentEnemy.hp <= 0) {
            battleOver = true;              // Enemy defeated
            currentState = GameState.EXPLORE; // Return to exploration mode
        }
    }

    // Player uses a skill that costs sanity points
    private void useSanitySkill() {
        if (player.sanity >= 10) {           // Check if player has enough sanity
            int damage = player.useSanitySkill(); // Perform skill attack
            currentEnemy.hp -= damage;       // Reduce enemy HP
            player.sanity -= 10;             // Deduct sanity cost
        } else {
            System.out.println("Not enough sanity!"); // Inform player if low sanity
        }
        playerTurn = false;                  // End player's turn
    }

    // Input Handling (Keyboard)

    @Override
    public void keyPressed(KeyEvent e) {
        // Only accept input during player's turn in battle and if battle is ongoing
        if (currentState == GameState.BATTLE && !battleOver && playerTurn) {
            if (e.getKeyCode() == KeyEvent.VK_A) {
                playerAttack();  // Player attacks enemy
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                useSanitySkill(); // Player uses sanity skill
            }
        }
    }

    // Unused but required by KeyListener interface
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
