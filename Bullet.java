import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class Bullet {
    
    private JPanel panel;
    private int x;
    private int y;
    private int width;
    private int height;
    private int dx;
    private int direction;
    private boolean active;
    private int damage;
    
    public Bullet(JPanel p, int xPos, int yPos, int dir) {
        panel = p;
        x = xPos;
        y = yPos;
        width = 5;
        height = 5;
        dx = 10;
        direction = dir;
        active = true;
        damage = 25;
    }
    
    public void draw(Graphics2D g2) {
        Ellipse2D.Double bullet = new Ellipse2D.Double(x, y, width, height);
        g2.setColor(Color.black);
        g2.fill(bullet);
    }
    
    public void move() {
        if (direction == 1) {
            x -= dx;
            if (x < 0) {
                active = false;
            }
        } else if (direction == 2) {
            x += dx;
            if (x > panel.getWidth()) {
                active = false;
            }
        }
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