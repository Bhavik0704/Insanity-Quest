import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BackgroundPanel extends JPanel {

    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            URL url = getClass().getResource(imagePath);
            if (url == null) {
                System.out.println("Background image not found: " + imagePath);
            } else {
                backgroundImage = new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(null); // allow absolute positioning
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
