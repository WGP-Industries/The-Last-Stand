import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class Fireball {
    private double x, y, dx, dy;
    private int width = 20, height = 20;
    private boolean active;
    private Image image;
    private Treasure treasure;
    private JPanel panel;

    public Fireball(JPanel p, int xPos, int yPos, Image img, Treasure trs, double fdx, double fdy) {
        panel = p;
        x = xPos;
        y = yPos;
        image = img;
        treasure = trs;
        dx = fdx;
        dy = fdy;
        active = true;
    }

    public void move() {
        x += dx;
        y += dy;

        if (x < 0 || x > panel.getWidth() || y < 0 || y > panel.getHeight()) {
            active = false;
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
            getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            treasure.takeDamage(15);
            active = false;
        }
    }

    public void draw(Graphics2D g2) {
        if (dx < 0) {
            g2.drawImage(image, (int) x + width, (int) y, -width, height, null);
        } else {
            g2.drawImage(image, (int) x, (int) y, width, height, null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}