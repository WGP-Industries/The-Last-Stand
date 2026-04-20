import javax.swing.JPanel;

/** Basic Bullet – standard projectile with moderate damage. Key: 1 */
public class BasicBullet extends AnimatedBullet {
    public BasicBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "bullet.png", 8, 80);
        damage = 25;
    }

    @Override public double getSpeed()   { return 10; }
    @Override public int    getCooldown() { return 250; }

    @Override
    public void onHit(Monster target, java.util.ArrayList<Monster> allMonsters) {
        target.takeDamage(damage);
    }
}