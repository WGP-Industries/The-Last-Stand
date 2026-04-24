import java.util.ArrayList;
import javax.swing.JPanel;

public class SpiritBullet extends AnimatedBullet {
    private static final int PUSH_AMOUNT = 160;

    public SpiritBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/wind_bullet.png", 4, 2, 80);
        damage = 5;
    }

    @Override public double getSpeed()    { return 13; }
    @Override public int    getCooldown() { return 300; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        if (target.isResistantToSprit()) {
            target.takeDamage(damage);
        } else {
            target.takeDamage(damage * 20);
        }

        target.push(PUSH_AMOUNT);
    }
}