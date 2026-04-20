import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.JPanel;

public class ExplosiveBullet extends AnimatedBullet {
    private static final int EXPLOSION_RADIUS = 150;
    private static final int EXPLOSION_TICKS  = 6;

    private boolean exploding   = false;
    private int     explodeTick = 0;

    public ExplosiveBullet(JPanel panel, int xPos, int yPos) {
        super(panel, xPos, yPos, "fire_bullet.png", 4, 80);
        damage = 50;
    }

    @Override public double  getSpeed()    { return 7; }
    @Override public int     getCooldown() { return 1200; }
    @Override public boolean isPiercing()  { return true; } // handles own lifecycle

    @Override
    public void move() {
        if (exploding) {
            if (--explodeTick <= 0) active = false;
            return;
        }
        super.move();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (exploding) {
            float progress = 1f - (float) explodeTick / EXPLOSION_TICKS;
            int   radius   = (int) (EXPLOSION_RADIUS * progress);
            int   alpha    = (int) (160 * (1f - progress));
            int   cx       = (int) x;
            int   cy       = (int) y;

            g2.setColor(new Color(255, 80, 0, Math.max(0, alpha)));
            g2.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
            g2.setColor(new Color(255, 200, 50, Math.max(0, alpha / 2)));
            g2.fill(new Ellipse2D.Double(cx - radius / 2, cy - radius / 2, radius, radius));
        } else {
            super.draw(g2);
        }
    }

    @Override
    public void onHit(Monster target, ArrayList<Monster> allMonsters) {
        if (exploding) return;

        exploding   = true;
        explodeTick = EXPLOSION_TICKS;

        double cx = x + width  / 2.0;
        double cy = y + height / 2.0;

        for (Monster m : allMonsters) {
            if (m.isDead()) continue;
            double mx = m.getX() + m.getBoundingRectangle().width  / 2.0;
            double my = m.getY() + m.getBoundingRectangle().height / 2.0;
            if (Math.hypot(mx - cx, my - cy) <= EXPLOSION_RADIUS) {
                m.takeDamage(damage);
            }
        }
    }
}