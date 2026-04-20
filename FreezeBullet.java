import java.util.ArrayList;
import javax.swing.JPanel;

public class FreezeBullet extends AnimatedBullet {
    private static final int FREEZE_TICKS = 4;

    public FreezeBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/ice_bullet.png", 10, 80);
        damage = 10;
    }

    @Override public double getSpeed()    { return 11; }
    @Override public int    getCooldown() { return 400; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        int actualDamage = (target instanceof SplitSlime) ? damage * 2 : damage;
        target.takeDamage(actualDamage);
        target.applyFreeze(FREEZE_TICKS);
    }
}