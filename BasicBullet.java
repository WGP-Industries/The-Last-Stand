import java.util.ArrayList;
import javax.swing.JPanel;

/** Basic Bullet – standard projectile with moderate damage. Key: 1 */
public class BasicBullet extends AnimatedBullet {
    public BasicBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/bullet.png", 8, 80);
        damage = 25;
    }

    @Override public double getSpeed()   { return 30; }
    @Override public int    getCooldown() { return 250; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        
        target.takeDamage(damage);
    }
}