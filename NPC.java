import java.awt.Graphics2D;

// Abstract base class representing a Non-Playable Character (NPC)
public abstract class NPC {
    // NPC's position on the map
    protected int x, y;

    // NPC's display name
    protected String name;

    // Constructor to initialize the NPC with a name and position
    public NPC(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    // Getter method for X position
    public int getX() { 
        return x; 
    }

    // Getter method for Y position
    public int getY() { 
        return y; 
    }

    // Abstract method that must be implemented to define how this NPC interacts with the player
    public abstract void interact(Player player, GamePanel game);

    // Abstract method that must be implemented to define how this NPC is drawn on the screen
    public abstract void draw(Graphics2D g2);
}
