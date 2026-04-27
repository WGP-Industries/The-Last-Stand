import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public class ElectricBullet extends AnimatedBullet {

    private static final int CHAIN_RANGE = 300;
    private static final int MAX_CHAINS  = 3;

    // FX stored so GameWindow can draw them via getChainFXList() / getShockedFX()
    private final List<ChainFX> chainFXList = new ArrayList<>();

    public ElectricBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/Thunder_projectile.png", 5, 80);
        damage = 20;
    }

    @Override public double getSpeed()    { return 100; }
    @Override public int    getCooldown() { return 300; }

    public List<ChainFX> getChainFXList() { return chainFXList; }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        if (target.isImmuneToElectricity()) return;

        target.takeDamage(damage);
        target.applyElectrocute(30);
        // trigger shocked FX on the monster itself
        target.electricFX.trigger(target.getX(), target.getY(),
                                  (int) target.getBoundingRectangle().width,
                                  (int) target.getBoundingRectangle().height);

        double cx = target.getX() + target.getBoundingRectangle().width  / 2.0;
        double cy = target.getY() + target.getBoundingRectangle().height / 2.0;

        List<Monster> chains = allMonsters.stream()
            .filter(m -> m != target && !m.isDead() && !m.isImmuneToElectricity())
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
        double prevCx = cx, prevCy = cy;

        for (Monster m : chains) {
            m.takeDamage(chainDamage);
            m.applyElectrocute(20);

            double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
            double my = m.getY() + m.getBoundingRectangle().height / 2.0;

            // shocked FX on each chained monster
            m.electricFX.trigger(m.getX(), m.getY(),
                                 (int) m.getBoundingRectangle().width,
                                 (int) m.getBoundingRectangle().height);

            // chain arc from previous monster (or initial target) to this one
            ChainFX arc = new ChainFX();
            arc.trigger(prevCx, prevCy, mx, my);
            chainFXList.add(arc);

            prevCx = mx;
            prevCy = my;
        }
    }
}