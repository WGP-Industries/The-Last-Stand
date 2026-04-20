import java.util.ArrayList;
import javax.swing.JPanel;

public class FreezeBullet extends AnimatedBullet {
    private static final int FREEZE_TICKS = 20;

    public FreezeBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "ice_bullet.png", 10, 80);
        damage = 8;
    }

    @Override public double getSpeed()    { return 11; }
    @Override public int    getCooldown() { return 400; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        target.takeDamage(damage);
        target.applyFreeze(FREEZE_TICKS);
    }
}