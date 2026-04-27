import java.util.ArrayList;
import javax.swing.JPanel;

public class TeleportBullet extends AnimatedBullet {
    public TeleportBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/space_bullet.png", 3, 80);
        damage = 0;
    }

    @Override public double getSpeed()    { return 30; }
    @Override public int    getCooldown() { return 350; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        target.respawn();
    }

    @Override
    public boolean bypassesShield() { return true; }
}