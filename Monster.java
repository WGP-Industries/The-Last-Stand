import java.awt.Color;
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

    // Burn status 
    private boolean burning              = false;
    private int     burnDamagePerStep    = 0;
    private int     burnStepsRemaining   = 0;
    private int     burnStepCounter      = 0;
    /** Apply burn damage every N ticks (4 × 50 ms = 200 ms between ticks). */
    private static final int BURN_TICK_INTERVAL = 4;

    // Freeze status
    private boolean frozen              = false;
    private int     freezeTicksRemaining = 0;
    private int     savedDx             = 0;


    public Monster(JPanel p, int xPos, int yPos, Player ply, Treasure trs, int dmg) {
        panel        = p;
        x            = xPos;
        y            = yPos;
        damage       = dmg;
        player       = ply;
        treasure     = trs;
        soundManager = SoundManager.getInstance();
        random       = new Random();
    }

    public void move() {
        if (!panel.isVisible()) return;

        applyStatusEffects();
        if (isDead()) return; // may have died from burn damage

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

    private void applyStatusEffects() {
        // Burn
        if (burning && !isDead()) {
            burnStepCounter++;
            if (burnStepCounter >= BURN_TICK_INTERVAL) {
                burnStepCounter = 0;
                takeDamage(burnDamagePerStep);
                burnStepsRemaining--;
                if (burnStepsRemaining <= 0) {
                    burning = false;
                }
            }
        }

        // Freeze: countdown
        if (frozen) {
            freezeTicksRemaining--;
            if (freezeTicksRemaining <= 0) {
                frozen = false;
                dx     = savedDx;
            }
        }
    }

    public void applyBurn(int dmgPerStep, int steps) {
        burning            = true;
        burnDamagePerStep  = dmgPerStep;
        burnStepsRemaining = steps;
        burnStepCounter    = 0;
    }

    public void applyFreeze(int ticks) {
        if (!frozen) {
            savedDx = dx; 
        }
        frozen               = true;
        freezeTicksRemaining = ticks;
        dx                   = 0;
    }


    public void push(int amount) {
        int travelDir = frozen ? savedDx : dx;
        if (travelDir < 0) {
            x += amount;
        } else if (travelDir > 0) {
            x -= amount; 
        }
    }

    public void respawnPublic() {
        respawn();
    }

    public void draw(Graphics2D g2) {
        if (dx <= 0) {
            g2.drawImage(monsterImageRight, x + width, y, -width, height, null);
        } else {
            g2.drawImage(monsterImageLeft, x, y, width, height, null);
        }
        drawStatusEffects(g2);
    }

    protected void drawStatusEffects(Graphics2D g2) {
        if (burning) {
            g2.setColor(new Color(255, 100, 0, 90));
            g2.fillRect(x, y, width, height);
        }
        if (frozen) {
            g2.setColor(new Color(100, 200, 255, 100));
            g2.fillRect(x, y, width, height);
        }
    }


    public void takeDamage(int amount) {
        if (isDead()) return;
        hp -= amount;
        if (isDead()) playDeathSound();
    }

    public void respawn() {
        int panelWidth = panel.getWidth();
        // Restore speed if frozen so monster starts moving again after respawn
        if (frozen) {
            frozen = false;
            dx     = savedDx;
        }
        x = (dx < 0) ? panelWidth + 50 : -50;
        y = 350;
        soundManager.playClip("appear", false);
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
    public int     getX()                              { return x; }
    public int     getY()                              { return y; }
}
