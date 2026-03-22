// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.JPanel;

// Abstract Monster class that serves as a base for all monster types, providing common properties and behaviors.
// I used the alien class for this, originally was just gonna have 1 monster but decided to make assignment 2 easier 
// by adding this.
public abstract class Monster {

    protected JPanel panel;
    protected int x, y, width, height, dx, dy, hp;
    protected Player player;
    protected Treasure treasure;
    protected SoundManager soundManager;
    protected Image monsterImageLeft;
    protected Image monsterImageRight;
    protected Random random;
    protected int damage ;

    public Monster(JPanel p, int xPos, int yPos, Player ply, Treasure trs, int dmg) {
        panel = p;
        x = xPos;
        y = yPos;
        damage = dmg;
        player = ply;
        treasure = trs;
        soundManager = SoundManager.getInstance();
        random = new Random();
    }

    public void move() {
        if (!panel.isVisible()) return;

        x += dx;
        y += dy;

        int panelWidth = panel.getWidth();


            // Collides with player
    if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
        collideWithPlayer();
        return;
    }

    // Collides with treasure
    if (treasure != null && !treasure.isDestroyed() &&
        getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
        collideWithTreasure();
    
        return;
    }


        // Off screen respawn
        if (x < -100 || x > panelWidth + 100 || y > panel.getHeight()) {
            respawn();
        }
    }

    public void respawn() {
        int panelWidth = panel.getWidth();
        x = (dx < 0) ? panelWidth + 50 : -50;
        y = 350;
        soundManager.playClip("appear", false);
    }

    public void draw() {
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;

         if (dx <= 0) {
            g2.drawImage(monsterImageRight, x + width, y, -width, height, null);
        } else {        
        g2.drawImage(monsterImageLeft, x, y, width, height, null);
        }


        g.dispose();
    }

    public void takeDamage(int damage) {
        hp -= damage;
        
        if (isDead() )playDeathSound();
    }

    
    public void collideWithTreasure() {
            if (treasure != null && !treasure.isDestroyed() &&
                getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
                treasure.takeDamage(damage);
                soundManager.playClip("hit", false);
                respawn();
            }
        }
    protected abstract void collideWithPlayer();
    
    // Each monster can have they own death sound, so VERY useful for assignment 2.
    protected abstract void playDeathSound();

    public boolean isDead()                        { return hp <= 0; }
    public int getHealth()                         { return hp; }
    public Rectangle2D.Double getBoundingRectangle() { return new Rectangle2D.Double(x, y, width, height); }
}