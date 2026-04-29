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

    protected final BurnFX burnFX = new BurnFX();
    protected final FreezeFX freezeFX = new FreezeFX();
    protected final FlickerDeathFX flickerDeathFX = new FlickerDeathFX();
    public final ElectricFX electricFX = new ElectricFX();

    private int aiTick = 0;
    private static final int AI_RETARGET_INTERVAL = 30;
    protected boolean facingLeft = false;
    protected boolean dying = false;

    private boolean burning = false;
    private int burnDamagePerStep = 0;
    private int burnStepsRemaining = 0;
    private int burnStepCounter = 0;
    private static final int BURN_TICK_INTERVAL = 4;

    protected int pushVelocity = 0;
    private static final double PUSH_FRICTION = 0.75;

    private boolean electricuted = false;

    private boolean frozen = false;
    private int freezeTicksRemaining = 0;
    private int savedDx = 0;

    protected SolidObjectManager solidObjectManager = null;

    private static final float GRAVITY = 0.60f;
    private static final float MAX_FALL_SPEED = 16f;
    private float gravityVelY = 0f;

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

    // Gravity

    protected boolean usesGravity() {
        return solidObjectManager != null;
    }

    protected void applyGravityAndPlatforms() {
        if (!usesGravity() || dying)
            return;

        gravityVelY = Math.min(gravityVelY + GRAVITY, MAX_FALL_SPEED);
        int newY = y + (int) gravityVelY;

        int landingY = (solidObjectManager != null)
                ? solidObjectManager.getLandingY(x, width, y, height)
                : WorldConfig.FLOOR_Y - height;

        if (newY >= landingY) {
            y = landingY;
            gravityVelY = 0f;
        } else {
            y = newY;
        }
    }

    public void move() {
        if (!panel.isVisible())
            return;

        applyStatusEffects();

        if (isDead() && !dying) {
            dying = true;
            if (deathLeftAnimation != null)
                deathLeftAnimation.start();
            if (deathRightAnimation != null)
                deathRightAnimation.start();
            playDeathSound();
        }

        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) {
                anim.update();
                if (!anim.isStillActive())
                    readyToRemove = true;
            } else {
                flickerDeathFX.tick();
                if (flickerDeathFX.isFinished())
                    readyToRemove = true;
            }
            return;
        }

        x += dx;
        y += dy;

        // Gravity
        applyGravityAndPlatforms();
        tickAI();

        if (dx != 0)
            facingLeft = dx < 0;

        updateWalkAnimation();

        if (sharedMonsterList != null)
            collideWithMonster(sharedMonsterList);

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
                getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
            return;
        }

        if (x < -2000 || x > WorldConfig.WORLD_W + 2000 || y > panel.getHeight()) {
            respawn();
        }
    }

    protected void faceTowardTreasure() {
        if (treasure == null || treasure.isDestroyed() || isDead())
            return;
        int tx = (int) treasure.getBoundingRectangle().getCenterX();
        int myCenter = x + width / 2;
        int speed = Math.max(1, Math.abs(dx));
        if (myCenter < tx && dx < 0) {
            dx = speed;
            facingLeft = false;
        } else if (myCenter > tx && dx > 0) {
            dx = -speed;
            facingLeft = true;
        }
    }

    protected void tickAI() {
        aiTick++;
        if (aiTick >= AI_RETARGET_INTERVAL) {
            aiTick = 0;
            faceTowardTreasure();
        }
    }

    private void updateWalkAnimation() {
        Animation anim = getWalkAnimation();
        if (anim != null)
            anim.update();
    }

    protected Animation getWalkAnimation() {
        return facingLeft ? walkLeftAnimation : walkRightAnimation;
    }

    protected Animation getDeathAnimation() {
        return facingLeft ? deathLeftAnimation : deathRightAnimation;
    }

    public boolean isImmuneToFire() {
        return false;
    }

    public boolean isImmuneToElectricity() {
        return false;
    }

    public boolean isImmuneToFreeze() {
        return false;
    }

    public boolean isResistantToSprit() {
        return true;
    }

    public boolean isResistantToPiercing() {
        return true;
    }

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
                if (burnStepsRemaining <= 0)
                    burning = false;
            }
        }

        if (frozen) {
            freezeTicksRemaining--;
            if (freezeTicksRemaining <= 0) {
                frozen = false;
                dx = savedDx;
            }
        }

        if (electricFX.isActive()) {
            electricFX.tick();
            // keep position locked to monster as it moves
            electricFX.updatePosition(x, y, width, height);
        }

        if (pushVelocity != 0) {
            x += pushVelocity;
            pushVelocity = (int) (pushVelocity * PUSH_FRICTION);
            if (Math.abs(pushVelocity) < 1) {
                pushVelocity = 0;
                if (!frozen && dx == 0 && savedDx != 0)
                    dx = savedDx;
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
                if (flickered != null)
                    g2.drawImage(flickered, x, y, width, height, null);
            }
            return;
        }

        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = frame.createGraphics();
        Animation walkAnim = getWalkAnimation();
        if (walkAnim != null)
            fg.drawImage(walkAnim.getImage(), 0, 0, width, height, null);
        else
            fg.drawImage(getImage(), 0, 0, width, height, null);
        fg.dispose();

        if (isBurning())
            frame = burnFX.applyToFrame(frame);
        if (isFrozen())
            frame = freezeFX.applyToFrame(frame);

        g2.drawImage(frame, x, y, width, height, null);

        if (electricFX.isActive())
            electricFX.draw(g2);

        drawHealthBar(g2);
    }

    protected Image getImage() {
        return facingLeft ? monsterImageLeft : monsterImageRight;
    }

    protected void drawHealthBar(Graphics2D g2) {
        if (isDead())
            return;
        int barX = x, barY = y - 10, barW = width, barH = 6;
        float pct = (maxHp > 0) ? Math.min(1f, Math.max(0f, (float) hp / maxHp)) : 0f;
        g2.setColor(new Color(180, 30, 30));
        g2.fillRect(barX, barY, barW, barH);
        g2.setColor(new Color(40, 180, 40));
        g2.fillRect(barX, barY, (int) (barW * pct), barH);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawRect(barX, barY, barW, barH);
    }

    public void applyBurn(int dmgPerStep, int steps) {
        burning = true;
        burnDamagePerStep = dmgPerStep;
        burnStepsRemaining = steps;
        burnStepCounter = 0;
    }

    public void applyFreeze(int ticks) {
        if (!frozen)
            savedDx = dx;
        frozen = true;
        freezeTicksRemaining = ticks;
        dx = 0;
    }

    public void applyElectrocute(int ticks) {
        electricuted = true;

    }

    public boolean isBurning() {
        return burning;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isElectrocuted() {
        return electricuted;
    }

    public void push(int amount) {
        int travelDir = frozen ? savedDx : dx;
        pushVelocity = (travelDir < 0) ? amount : -amount;
        if (!frozen) {
            savedDx = dx;
            dx = 0;
        }
    }

    public void heal(int amount) {
        if (isDead())
            return;
        hp = Math.min(hp + amount, maxHp);
    }

    public void takeDamage(int amount) {
        if (isDead())
            return;
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dying = true;
            if (deathLeftAnimation != null)
                deathLeftAnimation.start();
            if (deathRightAnimation != null)
                deathRightAnimation.start();
            playDeathSound();
        }
    }

    public void respawn() {
        int travelDx = frozen ? savedDx : dx;
        if (frozen) {
            frozen = false;
            dx = savedDx;
        }
        x = (travelDx < 0) ? WorldConfig.WORLD_W + 50 : -50;
        y = getY();
        if (sharedMonsterList != null)
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
        if (monsters == null)
            return;
        Rectangle2D.Double myBox = getBoundingRectangle();
        for (Monster m : monsters) {
            if (m == this || m.isDead())
                continue;
            if (!this.getClass().equals(m.getClass()))
                continue;
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

    protected int getSavedDx() {
        return savedDx;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int v) {
        dx = v;
    }

    public void setSolidObjectManager(SolidObjectManager mgr) {
        solidObjectManager = mgr;
    }

    protected abstract void collideWithPlayer();

    protected abstract void playDeathSound();

    public boolean isDead() {
        return hp <= 0;
    }

    public int getHealth() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}
