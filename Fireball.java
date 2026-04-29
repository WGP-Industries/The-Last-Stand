import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Fireball {

    private double x, y, dx, dy;
    private final int width = 20, height = 20;
    private boolean active;
    private final Image image;
    private final Treasure treasure;

    public Fireball(int xPos, int yPos, Image img, Treasure trs, double fdx, double fdy) {
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

        if (x < -100 || x > WorldConfig.WORLD_W + 100 || y < -100 || y > WorldConfig.FLOOR_Y + 100) {
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
        // flip the image horizontally when travelling left
        if (dx < 0) g2.drawImage(image, (int) x + width, (int) y, -width, height, null);
        else        g2.drawImage(image, (int) x,          (int) y,  width, height, null);
    }

    public boolean isActive() { return active; }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}