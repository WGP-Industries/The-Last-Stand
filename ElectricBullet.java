import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public class ElectricBullet extends AnimatedBullet {

    private static final int CHAIN_RANGE   = 300;
    private static final int MAX_CHAINS    = 3;
    private static final int CHAIN_DELAY   = 6;

    private boolean spent = false;
    private final List<ChainFX> chainFXList = new ArrayList<>();

    private final List<Monster> pendingChain = new ArrayList<>();
    private double prevCx, prevCy;
    private int chainTickTimer = 0;
    private int chainDamage    = 0;
    private boolean chainActive = false;

    public ElectricBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "images/bullets/Thunder_projectile.png", 5, 80);
        damage = 20;
    }

    @Override public double getSpeed()    { return 40; }
    @Override public int    getCooldown() { return 300; }

    public List<ChainFX> getChainFXList() { return chainFXList; }
    public boolean isChainActive() { return chainActive; }

    @Override
    public void move() {
        if (spent) return;
        super.move();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!spent) super.draw(g2);
    }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        if (spent || target.isDead() || target.isImmuneToElectricity()) return;

        spent = true;

        target.takeDamage(damage);
        target.applyElectrocute(30);
        target.electricFX.trigger(target.getX(), target.getY(),
                (int) target.getBoundingRectangle().width,
                (int) target.getBoundingRectangle().height);

        double cx = target.getX() + target.getBoundingRectangle().width  / 2.0;
        double cy = target.getY() + target.getBoundingRectangle().height / 2.0;

        chainDamage = Math.max(1, damage / 2);
        prevCx = cx;
        prevCy = cy;

        pendingChain.clear();
        pendingChain.addAll(
            allMonsters.stream()
                .filter(m -> {
                    if (m == target || m.isDead() || m.isImmuneToElectricity()) return false;
                    double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
                    double my = m.getY() + m.getBoundingRectangle().height / 2.0;
                    return Math.hypot(mx - cx, my - cy) <= CHAIN_RANGE;
                })
                .sorted(Comparator.comparingDouble(m -> {
                    double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
                    double my = m.getY() + m.getBoundingRectangle().height / 2.0;
                    return Math.hypot(mx - cx, my - cy);
                }))
                .limit(MAX_CHAINS)
                .collect(Collectors.toList())
        );

        chainTickTimer = CHAIN_DELAY;
        chainActive = !pendingChain.isEmpty();
    }

    public boolean isSpent() { return spent; }

    public boolean isFinished() {
        return spent && !chainActive && chainFXList.isEmpty();
    }

    public void tickChain() {
        chainFXList.removeIf(fx -> { fx.tick(); return !fx.isActive(); });

        if (!chainActive) return;

        chainTickTimer--;
        if (chainTickTimer > 0) return;

        if (pendingChain.isEmpty()) {
            chainActive = false;
            return;
        }

        Monster m = pendingChain.remove(0);
        double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
        double my = m.getY() + m.getBoundingRectangle().height / 2.0;

        if (!m.isDead()) {
            m.takeDamage(chainDamage);
            m.applyElectrocute(20);
            m.electricFX.trigger(m.getX(), m.getY(),
                    (int) m.getBoundingRectangle().width,
                    (int) m.getBoundingRectangle().height);

            ChainFX arc = new ChainFX();
            arc.trigger(prevCx, prevCy, mx, my);
            chainFXList.add(arc);
        }

        prevCx = mx;
        prevCy = my;

        chainTickTimer = CHAIN_DELAY;
        if (pendingChain.isEmpty()) chainActive = false;
    }
}