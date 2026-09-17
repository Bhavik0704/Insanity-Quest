package finalRPG;

import java.util.Random;

public class Enemy {
	// Classifying enemy HP
    int hp = 60;

    // Giving the enemy an attack stat to do a random amount of damage
    public int attack() {
        return 5 + new Random().nextInt(10);
    }
}
