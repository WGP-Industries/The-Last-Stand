import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;


public abstract class Monster {

    protected JPanel panel;

    public List<Monster> sharedMonsterList;

    protected int x, y, width, height, dx, dy, hp;
    protected Player player;
    protected Treasure treasure;
    protected SoundManager soundManager;
    protected Image monsterImageLeft;
    protected Image monsterImageRight;
    protected Random random;
    protected int damage;
    protected int maxHp;
    protected boolean readyToRemove = false;

    protected Animation walkLeftAnimation;
    protected Animation walkRightAnimation;
    protected Animation deathLeftAnimation;
    protected Animation deathRightAnimation;


    protected  final BurnFX burnFX = new BurnFX();
    protected  final FreezeFX freezeFX = new FreezeFX();
    protected final FlickerDeathFX flickerDeathFX = new FlickerDeathFX();
    

    protected boolean facingLeft = false;
    protected boolean dying = false;

    private boolean burning = false;
    private int burnDamagePerStep = 0;
    private int burnStepsRemaining = 0;
    private int burnStepCounter = 0;
    private static final int BURN_TICK_INTERVAL = 4;

  

    private boolean electricuted = false;
    private int electrocuteTicksRemaining = 0;

    


    private boolean frozen = false;
    private int freezeTicksRemaining = 0;
    private int savedDx = 0;

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

        applyStatusEffects();

        if (isDead() && !dying) {
            dying = true;
            if (deathLeftAnimation != null) deathLeftAnimation.start();
            if (deathRightAnimation != null) deathRightAnimation.start();
            playDeathSound();
        }

        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) {
                anim.update();
                if (!anim.isStillActive()) {
                    readyToRemove = true;
                }
            } else {
                flickerDeathFX.tick();
                if (flickerDeathFX.isFinished()) readyToRemove = true;
            }
            return;
        }

        x += dx;
        y += dy;

        if (dx != 0) facingLeft = dx < 0;

        updateWalkAnimation();

        int panelWidth = panel.getWidth();
        if (sharedMonsterList != null) collideWithMonster(sharedMonsterList);

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
            getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
            return;
        }

       if (x < -2000 || x > panelWidth + 2000 || y > panel.getHeight()) {
                respawn();
            }
    }

    private void updateWalkAnimation() {
        Animation anim = getWalkAnimation();
        if (anim != null) anim.update();
    }

    protected Animation getWalkAnimation() {
        return facingLeft ? walkLeftAnimation : walkRightAnimation;
    }

    protected Animation getDeathAnimation() {
        return facingLeft ? deathLeftAnimation : deathRightAnimation;
    }

    public boolean isImmuneToFire() { return false; }
    public boolean isImmuneToElectricity() { return false; }
    public boolean isImmuneToFreeze() { return false; }
    public boolean isResistantToSprit() { return true; }
    public boolean isResistantToPiercing() { return true; }
   

    public boolean isReadyToRemove() {
        return readyToRemove;
    }

    protected void applyStatusEffects() {
        if (burning && !isDead()) {
            burnStepCounter++;
            if (burnStepCounter >= BURN_TICK_INTERVAL) {
                burnStepCounter = 0;
                takeDamage(burnDamagePerStep);
                burnStepsRemaining--;
                if (burnStepsRemaining <= 0) burning = false;
            }
        }

        if (frozen) {
            freezeTicksRemaining--;
            if (freezeTicksRemaining <= 0) {
                frozen = false;
                dx = savedDx;
            }
        }

        if (electricuted) {
            electrocuteTicksRemaining--;
            if (electrocuteTicksRemaining <= 0) {
                electricuted = false;
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (dying) {
                    Animation deathAnim = getDeathAnimation();
                    if (deathAnim != null) {
                        g2.drawImage(deathAnim.getImage(), x, y, width, height, null);
                    } else {
                        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D fg = base.createGraphics();
                        fg.drawImage(getImage(), 0, 0, width, height, null);
                        fg.dispose();

                        BufferedImage flickered = flickerDeathFX.applyToFrame(base);
                        if (flickered != null) {
                            g2.drawImage(flickered, x, y, width, height, null);
                        }
                    }
                    return;
                }

        // Render monster into an offscreen frame once
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = frame.createGraphics();
        Animation walkAnim = getWalkAnimation();
        if (walkAnim != null) {
            fg.drawImage(walkAnim.getImage(), 0, 0, width, height, null);
        } else {
            fg.drawImage(getImage(), 0, 0, width, height, null);
        }
        fg.dispose();

        // Apply effects to the frame, chained so both can stack
        if (isBurning()) frame = burnFX.applyToFrame(frame);
        if (isFrozen())  frame = freezeFX.applyToFrame(frame);

        // Single draw to screen
        g2.drawImage(frame, x, y, width, height, null);

        drawHealthBar(g2);
    }



    protected Image getImage() {
        return facingLeft ? monsterImageLeft : monsterImageRight;
    }



    protected void drawHealthBar(Graphics2D g2) {
        if (isDead()) return;
        int barWidth = width;
        int barHeight = 6;
        int barX = x;
        int barY = y - 10;

        float pct = (maxHp > 0) ? Math.min(1f, Math.max(0f, (float) hp / maxHp)) : 0f;

        g2.setColor(new Color(180, 30, 30));
        g2.fillRect(barX, barY, barWidth, barHeight);

        g2.setColor(new Color(40, 180, 40));
        g2.fillRect(barX, barY, (int)(barWidth * pct), barHeight);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawRect(barX, barY, barWidth, barHeight);
    }

    public void applyBurn(int dmgPerStep, int steps) {
        burning = true;
        burnDamagePerStep = dmgPerStep;
        burnStepsRemaining = steps;
        burnStepCounter = 0;
    }

    public void applyFreeze(int ticks) {
        if (!frozen) savedDx = dx;
        frozen = true;
        freezeTicksRemaining = ticks;
        dx = 0;
    }

    public void applyElectrocute(int ticks) {
        electricuted = true;
        electrocuteTicksRemaining = ticks;
    }


    





    public boolean isBurning() { return burning; }
    public boolean isFrozen() { return frozen; }
    public boolean isElectrocuted() { return electricuted; }

    public void push(int amount) {
        int travelDir = frozen ? savedDx : dx;
        if (travelDir < 0) x += amount;
        else if (travelDir > 0) x -= amount;
    }


    public void heal(int amount) {
        if (isDead()) return;
        hp = Math.min(hp + amount, maxHp);
    }

    public void takeDamage(int amount) {
        if (isDead()) return;
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dying = true;
            if (deathLeftAnimation != null) deathLeftAnimation.start();
            if (deathRightAnimation != null) deathRightAnimation.start();
            playDeathSound();
        }
    }

    public void respawn() {
        int panelWidth = panel.getWidth() == 0 ? 800 : panel.getWidth();
        int travelDx = frozen ? savedDx : dx;
        if (frozen) {
            frozen = false;
            dx = savedDx;
        }
        x = (travelDx < 0) ? panelWidth + 50 : -50;
        y = getY();

        collideWithMonster(sharedMonsterList);
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

    public void collideWithMonster(List<Monster> monsters) {
        Rectangle2D.Double myBox = getBoundingRectangle();

        for (Monster m : monsters) {
            if (m == this || m.isDead()) continue;
            if (!this.getClass().equals(m.getClass())) continue;

            Rectangle2D.Double otherBox = m.getBoundingRectangle();

            if (myBox.intersects(otherBox)) {
                if (this.x < m.x) {
                    this.x -= 30;
                    m.x += 30;
                } else {
                    this.x += 30;
                    m.x -= 30;
                }
                myBox = getBoundingRectangle();
            }
        }
    }


    protected int getSavedDx() { return savedDx; }

    protected abstract void collideWithPlayer();
    protected abstract void playDeathSound();

    public boolean isDead() { return hp <= 0; }
    public int getHealth() { return hp; }
    public Rectangle2D.Double getBoundingRectangle() { return new Rectangle2D.Double(x, y, width, height); }
    public int getX() { return x; }
    public int getY() { return y; }
}