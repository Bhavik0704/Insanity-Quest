
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Create the main game window (JFrame) with the title "Insanity Quest"
        JFrame window = new JFrame("Insanity Quest");

        // Ensures the application exits when the window is closed
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Prevent the player from resizing the window (maintains consistent tile layout)
        window.setResizable(false);

        // Create an instance of the main game panel (which handles rendering and input)
        GamePanel gamePanel = new GamePanel();

        // Add the game panel to the window
        window.add(gamePanel);

        // Sizes the window to fit the preferred size of its components (from GamePanel)
        window.pack();

        // Center the window on the screen
        window.setLocationRelativeTo(null);

        // Make the window visible
        window.setVisible(true);

        // Start the game loop thread inside the GamePanel
        gamePanel.startGameThread();
    }
}
