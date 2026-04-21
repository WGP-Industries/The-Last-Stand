import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public class ElectricBullet extends AnimatedBullet {
    private static final int CHAIN_RANGE = 220;
    private static final int MAX_CHAINS  = 2;

    public ElectricBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/Thunder_projectile.png", 5, 80);
        damage = 20;
    }

    @Override public double getSpeed()    { return 12; }
    @Override public int    getCooldown() { return 300; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        target.takeDamage(damage);

        double cx = target.getX() + target.getBoundingRectangle().width  / 2.0;
        double cy = target.getY() + target.getBoundingRectangle().height / 2.0;

        List<Monster> chains = allMonsters.stream()
            .filter(m -> m != target && !m.isDead())
            .sorted(Comparator.comparingDouble(m -> {
                double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
                double my = m.getY() + m.getBoundingRectangle().height / 2.0;
                return Math.hypot(mx - cx, my - cy);
            }))
            .filter(m -> {
                double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
                double my = m.getY() + m.getBoundingRectangle().height / 2.0;
                return Math.hypot(mx - cx, my - cy) <= CHAIN_RANGE;
            })
            .limit(MAX_CHAINS)
            .collect(Collectors.toList());

        int chainDamage = Math.max(1, damage / 2);
        for (Monster m : chains) m.takeDamage(chainDamage);
    }
}