import java.util.ArrayList;
import javax.swing.JPanel;

public class RapidBullet extends AnimatedBullet {
    public RapidBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/bullet.png", 8, 60);
        damage = 12;
    }

    @Override public double getSpeed()    { return 70; }
    @Override public int    getCooldown() { return 80; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        target.takeDamage(damage);
    }
}