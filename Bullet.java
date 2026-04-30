import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JPanel;

// Base class for bullets. It defines common properties and methods for all bullet types,
public abstract class Bullet {

    protected JPanel panel;
    protected double x, y; // double for smooth diagonal movement
    protected int width, height;
    protected double vx, vy; // velocity set by Player.shoot()
    protected boolean active;
    protected int damage;
    protected double prevX, prevY;

    public Bullet(JPanel panel, int xPos, int yPos) {
        this.panel = panel;
        this.x = xPos;
        this.y = yPos;
        this.active = true;
        this.width = 5;
        this.height = 5;
    }

    public abstract void draw(Graphics2D g2);

    public abstract void onHit(Monster target, ArrayList<Monster> allMonsters);

    public abstract double getSpeed();

    public void move() {
        prevX = x;
        prevY = y;

        x += vx;
        y += vy;

        if (x + width < 0
                || x > WorldConfig.WORLD_W
                || y + height < 0
                || y > panel.getHeight()) {
            active = false;
        }
    }

    public boolean bypassesShield() {
        return false;
    }

    public boolean isPiercing() {
        return false;
    }

    public int getCooldown() {
        return 250;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public int getDamage() {
        return damage;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}