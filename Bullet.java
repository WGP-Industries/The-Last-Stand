// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Color;
import java.awt.Graphics;
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
    private int dx ;  // vertical movement speed
    private int direction;
    private boolean active;
    private int damage; // damage dealt to monsters
    
    public Bullet(JPanel p, int xPos, int yPos, int dir) {
        panel = p;
        x = xPos;
        y = yPos;
        width = 5;   // 5 pixel wide
        height = 5;  // 5 pixel tall
        dx = 10 ;   
        direction = dir;
        active = true;
        damage = 25; // default damage
    }
    
    public void draw() {
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        
        Ellipse2D.Double bullet = new Ellipse2D.Double(x, y, width, height);
        g2.setColor(Color.black);  
        g2.fill(bullet);
        
        g.dispose();
    }
    
public void move() {
    if (direction == 1) {  // moving left
        x -= dx;
        if (x < 0) {
            active = false;
        }
    } else if (direction == 2) {  // moving right
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