import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InsanityQuestGUI {
    // Main JFrame for the title screen
    private JFrame frame;

    // JPanel used to draw a black fade overlay (for fade-in effect)
    private JPanel fadePanel;

    // Opacity level of the fade overlay (1.0 = fully opaque black, 0.0 = fully transparent)
    private float fadeOpacity = 1.0f;

    public InsanityQuestGUI() {
        // Initialize the main frame/window with title "Insanity Quest"
        frame = new JFrame("Insanity Quest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Close program on window close
        frame.setSize(800, 600);                                // Set fixed window size
        frame.setLocationRelativeTo(null);                      // Center window on screen

        // Load the background image from resources folder
        ImageIcon backgroundIcon = new ImageIcon("resources/title_background.jpg"); // TODO: update path if needed
        // Create a JLabel to hold the background image
        JLabel backgroundLabel = new JLabel(backgroundIcon);

        // Set the layout manager of the label to BoxLayout on Y_AXIS (vertical stacking)
        // This allows stacking components vertically centered on the image
        backgroundLabel.setLayout(new BoxLayout(backgroundLabel, BoxLayout.Y_AXIS));

        // Add vertical glue to push following components toward vertical center
        backgroundLabel.add(Box.createVerticalGlue());

        // Create and configure the game title JLabel
        JLabel titleLabel = new JLabel("Insanity Quest");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);         // Center horizontally in BoxLayout
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48));          // Large serif bold font for title
        titleLabel.setForeground(Color.BLACK);                         // Title text color black
        titleLabel.setOpaque(false);                                   // Transparent background (show image)
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));  // Padding above and below
        backgroundLabel.add(titleLabel);                               // Add title to background label

        // Create the Start Game button
        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);         // Center button horizontally
        startButton.setFont(new Font("SansSerif", Font.BOLD, 32));     // Large sans serif font for button
        startButton.setBackground(Color.BLACK);                         // Button background color black
        startButton.setForeground(Color.WHITE);                         // Button text color white
        startButton.setFocusPainted(false);                             // Remove focus border painting for cleaner look

        // Add an action listener for button clicks
        startButton.addActionListener(e -> {
            // Close the title screen window when Start Game is clicked
            frame.dispose();

            // Launch the Zone1 GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                JFrame zone1Frame = new JFrame("Zone 1");
                zone1Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                zone1Frame.setContentPane(new Zone1GUI()); // Replace with actual Zone1 GUI panel
                zone1Frame.pack();                         // Pack frame to fit contents
                zone1Frame.setLocationRelativeTo(null);   // Center new window on screen
                zone1Frame.setVisible(true);               // Show the Zone1 window
            });
        });

        // Add the start button below the title label
        backgroundLabel.add(startButton);

        // Add another vertical glue to push content toward center vertically
        backgroundLabel.add(Box.createVerticalGlue());

        // Set the content pane of the frame to the background label with title and button
        frame.setContentPane(backgroundLabel);

        // Create a fade overlay panel to draw a black rectangle with adjustable transparency
        fadePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Set the color to black with current fadeOpacity alpha
                g2d.setColor(new Color(0, 0, 0, Math.min(1f, fadeOpacity)));
                // Fill entire panel with this translucent black color
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        fadePanel.setOpaque(false);                     // Make panel non-opaque so background is visible
        fadePanel.setBounds(0, 0, 800, 600);            // Set panel size to cover entire frame

        // Add fadePanel on top of everything in the layered pane at the DRAG_LAYER level
        // This ensures fadePanel is drawn over the content pane
        frame.getLayeredPane().add(fadePanel, JLayeredPane.DRAG_LAYER);

        // Create a timer to gradually decrease fadeOpacity to 0 (fade-in effect)
        Timer fadeTimer = new Timer(40, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fadeOpacity -= 0.02f;            // Reduce opacity by 0.02 each tick (~40ms)
                if (fadeOpacity <= 0f) {         // When fully transparent,
                    fadeOpacity = 0f;
                    ((Timer) e.getSource()).stop();  // stop the timer to end the fade-in
                }
                fadePanel.repaint();              // Repaint fadePanel to update fade effect
            }
        });
        fadeTimer.start();                       // Start fade-in animation timer

        // Make the main title frame visible
        frame.setVisible(true);
    }

    // Main method to launch the GUI application on the Event Dispatch Thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(InsanityQuestGUI::new);
    }
}
