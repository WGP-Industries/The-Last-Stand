import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.JPanel;

public abstract class Monster {

    protected JPanel panel;
    protected int x, y, width, height, dx, dy, hp;
    protected Player player;
    protected Treasure treasure;
    protected SoundManager soundManager;
    protected Image monsterImageLeft;
    protected Image monsterImageRight;
    protected Random random;
    protected int damage;

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

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
            getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
            return;
        }

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

    public void draw(Graphics2D g2) {
        if (dx <= 0) {
            g2.drawImage(monsterImageRight, x + width, y, -width, height, null);
        } else {
            g2.drawImage(monsterImageLeft, x, y, width, height, null);
        }
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (isDead()) playDeathSound();
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
    protected abstract void playDeathSound();

    public boolean isDead()                           { return hp <= 0; }
    public int getHealth()                            { return hp; }
    public Rectangle2D.Double getBoundingRectangle()  { return new Rectangle2D.Double(x, y, width, height); }
}