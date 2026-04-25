import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

public class Healer extends Monster {

    private static final int STOP_DISTANCE = 330;
    private static final int TREASURE_DAMAGE = 200;
    private static final int HEAL_COOLDOWN = 60;
    private static final int HEAL_LINGER_TIME = 20;

    private enum Phase { WALKING, CHARGING_STRONG, CHARGING_WEAK, LINGER, COOLDOWN, ROGUE }
    private Phase phase = Phase.WALKING;

    private Animation strongHealAnimation;
    private Animation weakHealAnimation;
    private Animation strongHealLeftAnimation;
    private Animation weakHealLeftAnimation;

    private int healCooldown = 0;
    private int lingerTimer = 0;

    private boolean lastHealWasStrong = true;
    private boolean hasEnteredScreen = false;

    public Healer(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 0);

        width = 60;
        height = 70;
        hp = 80;
        maxHp = hp;

        dx = (xPos < 0) ? 2 : -2;
        facingLeft = (dx < 0);

        walkLeftAnimation = new Animation(true);
        walkRightAnimation = new Animation(true);
        deathLeftAnimation = new Animation(false);
        deathRightAnimation = new Animation(false);

        strongHealAnimation = new Animation(false);
        weakHealAnimation = new Animation(false);
        strongHealLeftAnimation = new Animation(false);
        weakHealLeftAnimation = new Animation(false);

        for (int i = 1; i <= 8; i++) {
            walkRightAnimation.addFrame(ImageManager.loadImage("images/healer/right_walk/healer_right_walk_" + i + ".png"), 100);
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/healer/left_walk/healer_left_walk_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 10; i++) {
            deathLeftAnimation.addFrame(ImageManager.loadImage("images/healer/dead/left_dead/healer_left_death_" + i + ".png"), 100);
            deathRightAnimation.addFrame(ImageManager.loadImage("images/healer/dead/right_dead/healer_right_death_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 3; i++) {
            strongHealAnimation.addFrame(ImageManager.loadImage("images/healer/strong_heal/healer_right_strong_heal_" + i + ".png"), 120);
            strongHealLeftAnimation.addFrame(ImageManager.loadImage("images/healer/strong_heal/healer_left_strong_heal_" + i + ".png"), 120);

            weakHealAnimation.addFrame(ImageManager.loadImage("images/healer/weak_heal/healer_right_weak_heal_" + i + ".png"), 120);
            weakHealLeftAnimation.addFrame(ImageManager.loadImage("images/healer/weak_heal/healer_left_weak_heal_" + i + ".png"), 120);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    private Animation getStrongHealAnimation() {
        return facingLeft ? strongHealLeftAnimation : strongHealAnimation;
    }

    private Animation getWeakHealAnimation() {
        return facingLeft ? weakHealLeftAnimation : weakHealAnimation;
    }

    private boolean hasValidTargets(ArrayList<Monster> allMonsters) {
        for (Monster m : allMonsters) {
            if (m == this || m.isDead()) continue;
            if (!(m instanceof Healer)) return true;
        }
        return false;
    }

    private void healAll(ArrayList<Monster> allMonsters, int amount) {
        for (Monster m : allMonsters) {
            if (m == this || m.isDead() || m instanceof Healer) continue;
            m.heal(amount);
        }
    }

    public void move(ArrayList<Monster> allMonsters) {
        if (!panel.isVisible()) return;
        
        applyStatusEffects();
        
        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) {
                anim.update();
                if (!anim.isStillActive()) readyToRemove = true;
            } else {
                readyToRemove = true;
            }
            return;
        }

        if (!hasEnteredScreen) {
            x += dx;
            if (dx != 0) facingLeft = (dx < 0);
            getWalkAnimation().update();
            if (x >= 0 && x <= panel.getWidth()) hasEnteredScreen = true;
            return;
        }

        if (phase != Phase.ROGUE && !hasValidTargets(allMonsters)) {
            phase = Phase.ROGUE;
        }

        

          if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
        collideWithPlayer();
        return;

             }

        switch (phase) {

            case WALKING:
                if (treasure != null && !treasure.isDestroyed()) {
                    int tx = (int) treasure.getBoundingRectangle().getCenterX();
                    if (Math.abs(x - tx) <= STOP_DISTANCE) {
                        getStrongHealAnimation().start();
                        phase = Phase.CHARGING_STRONG;
                        break;
                    }
                }

                x += dx;
                facingLeft = (dx < 0);
                getWalkAnimation().update();
                break;

            case CHARGING_STRONG:
                getStrongHealAnimation().update();
                if (!getStrongHealAnimation().isStillActive()) {
                    if (hasValidTargets(allMonsters)) healAll(allMonsters, 30);
                    lastHealWasStrong = true;
                    lingerTimer = HEAL_LINGER_TIME;
                    phase = Phase.LINGER;
                }
                break;

            case CHARGING_WEAK:
                getWeakHealAnimation().update();
                if (!getWeakHealAnimation().isStillActive()) {
                    if (hasValidTargets(allMonsters)) healAll(allMonsters, 10);
                    lastHealWasStrong = false;
                    lingerTimer = HEAL_LINGER_TIME;
                    phase = Phase.LINGER;
                }
                break;

            case LINGER:
                lingerTimer--;
                if (lingerTimer <= 0) {
                    healCooldown = HEAL_COOLDOWN;
                    phase = Phase.COOLDOWN;
                }
                break;

            case COOLDOWN:
                healCooldown--;
                if (healCooldown <= 0) {
                    if (lastHealWasStrong) {
                        getWeakHealAnimation().start();
                        phase = Phase.CHARGING_WEAK;
                    } else {
                        getStrongHealAnimation().start();
                        phase = Phase.CHARGING_STRONG;
                    }
                }
                break;

            case ROGUE:
    x += dx;
    getWalkAnimation().update();

    if (treasure != null &&
        getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
        treasure.takeDamage(TREASURE_DAMAGE);
        hp = 0;
        dying = true;
        facingLeft = (dx < 0);
        if (deathLeftAnimation != null) deathLeftAnimation.start();
        if (deathRightAnimation != null) deathRightAnimation.start();
        playDeathSound();
    }
    break;
        }
    }

  
        @Override
        public void draw(Graphics2D g2) {
            if (dying) {
                super.draw(g2);
                return;
            }

            Image raw;
            switch (phase) {
                case CHARGING_STRONG, LINGER -> raw = getStrongHealAnimation().getImage();
                case CHARGING_WEAK           -> raw = getWeakHealAnimation().getImage();
                default                      -> raw = getWalkAnimation().getImage();
            }

            BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = frame.createGraphics();
            fg.drawImage(raw, 0, 0, width, height, null);
            fg.dispose();

            if (isBurning()) frame = burnFX.applyToFrame(frame);
            if (isFrozen())  frame = freezeFX.applyToFrame(frame);

            g2.drawImage(frame, x, y, width, height, null);
            drawHealthBar(g2);
        }





    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            phase = Phase.WALKING;
            respawn();
        }
    }

    @Override
    protected void playDeathSound() {
        soundManager.playClip("die", false);
    }
}