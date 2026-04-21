import java.util.ArrayList;
import javax.swing.JPanel;

public class FireBullet extends AnimatedBullet {
    private static final int BURN_DMG_PER_STEP = 8;
    private static final int BURN_STEPS        = 6;

    public FireBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/fire_bullet.png", 4, 80);
        damage = 10;
    }

    @Override public double getSpeed()    { return 9; }
    @Override public int    getCooldown() { return 350; }

   @Override
public void onHit(Monster target, ArrayList<Monster> allMonsters) {
    if (target.isImmuneToFire()) return;
    int actualDamage = (target instanceof SplitSlime) ? damage * 2 : damage;
    target.takeDamage(actualDamage);
    target.applyBurn(BURN_DMG_PER_STEP, BURN_STEPS);
}
}