// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;


// Treasure class representing the treasure that the player must defend.
public class Treasure {

    private JPanel panel;
    private int x, y, size;
    private int hp, maxHp;
    private boolean destroyed;

    public Treasure(JPanel p, int xPos, int yPos) {
        panel = p;
        size = 40;
        x = xPos - size / 2; 
        y = yPos;
        maxHp = 100;
        hp = maxHp;
        destroyed = false;
    }

  
// Draws the treasure on the panel with a health bar
    public void draw() {
        if (destroyed) return;

        Graphics g = panel.getGraphics();
        if (g == null) return;
        Graphics2D g2 = (Graphics2D) g;

        // Colour shifts cyan to red as HP drops, looks cool to me :).
        float ratio = (float) hp / maxHp;
        Color gemColor = new Color(1 - ratio, ratio * 0.8f, ratio);

        

        // sets the colour of the gem based on its health, and draws it as a circle
        g2.setColor(gemColor);
        g2.fill(new Ellipse2D.Double(x, y, size, size));
        g2.setColor(Color.WHITE);
        g2.draw(new Ellipse2D.Double(x, y, size, size));

        
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x - 5, y + size + 5, size + 10, 8);

    
        g2.setColor(new Color(1 - ratio, ratio, 0f));
        g2.fillRect(x - 5, y + size + 5, (int)((size + 10) * ratio), 8);

        
        g2.setColor(Color.WHITE);
        g2.drawRect(x - 5, y + size + 5, size + 10, 8);

        g.dispose();
    }

    public void takeDamage(int damage) {
        if (destroyed) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            destroyed = true;
        }
    }

    public boolean isDestroyed() { return destroyed; }
    public int getHp()           { return hp; }
    public int getMaxHp()        { return maxHp; }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, size, size);
    }
}