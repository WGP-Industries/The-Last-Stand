import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;

public class PiercingBullet extends AnimatedBullet {

  
    
    private final Set<Monster> hitMonsters = new HashSet<>();
    private static final int critChance = 20; // 20% chance for critical hit
  
    public PiercingBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/bullet.png", 8, 80);
        damage = 30;
        
    }

    @Override public double  getSpeed()    { return 35; }
    @Override public int     getCooldown() { return 400; }
    @Override public boolean isPiercing()  { return true; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        if (!target.isResistantToPiercing()){
            if (Math.random() * 100 < critChance) {
                int damageMultiplier = new Random().nextInt(2, 6);
                damage = damage * damageMultiplier; // Critical hit
            } 
        } else {
            damage = damage / 2;
        }

        if (hitMonsters.contains(target)) return;
        hitMonsters.add(target);
        target.takeDamage(damage);
    }
}