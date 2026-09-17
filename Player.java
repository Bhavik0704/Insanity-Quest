import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Player {
    int hp = 100;
    int sanity = 100;

    public void update() {
        // movement or interaction update
    }

    public void draw(Graphics g) {
        g.setColor(Color.green);
        g.fillRect(100, 100, 48, 48); // simple representation
    }

    public int attack() {
        return 10 + new Random().nextInt(6); // 10-15 damage
    }

    public int useSanitySkill() {
        return 2 * attack(); // Powerful skill
    }
}
