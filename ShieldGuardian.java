import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ShieldGuardian extends Monster {

    private static final int STOP_DISTANCE = 250;

    private Shield shield;
    private boolean reachedStop;
    private int stopX;

    public ShieldGuardian(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 50);

        width  = 70;
        height = 80;
        hp     = 300;
        maxHp  = hp;
        dx     = (xPos < 0) ? 2 : -2;
        dy     = 0;

        facingLeft   = (dx < 0);
        reachedStop  = false;
        stopX        = -1;

        shield = new Shield();

        walkLeftAnimation  = new Animation(true);
        walkRightAnimation = new Animation(true);

        for (int i = 1; i <= 3; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/shield_guardian/shield_guardian_left_walk_" + i + ".png"), 150);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/shield_guardian/shield_guardian_right_walk_" + i + ".png"), 150);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    private void initStopX() {
        if (stopX == -1) {
            int tx = (int) treasure.getBoundingRectangle().getCenterX();
            stopX = (dx > 0) ? tx - STOP_DISTANCE : tx + STOP_DISTANCE;
        }
    }

    public Shield getShield() { return shield; }

    @Override
    public void move() {
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

        initStopX();

        if (!reachedStop) {
            x += dx;
            facingLeft = (dx < 0);
            getWalkAnimation().update();

            if ((dx > 0 && x >= stopX) || (dx < 0 && x <= stopX)) {
                x           = stopX;
                dx          = 0;
                reachedStop = true;
            }
        }

        // Gravity
        applyGravityAndPlatforms();

        shield.update(x, y, width, facingLeft);

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
            getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
        }
    }

    @Override
    public void takeDamage(int amount) {
            if (dying) return;
            hp -= amount;
            if (hp <= 0) {
                hp = 0;
                dying = true;
                facingLeft = reachedStop ? facingLeft : (dx <= 0);
                if (deathLeftAnimation  != null) deathLeftAnimation.start();
                if (deathRightAnimation != null) deathRightAnimation.start();
                playDeathSound();
            }
        }

    @Override
    public void draw(Graphics2D g2) {
        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) g2.drawImage(anim.getImage(), x, y, width, height, null);
            return;
        }

        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = frame.createGraphics();
        fg.drawImage(getWalkAnimation().getImage(), 0, 0, width, height, null);
        fg.dispose();

        if (isBurning()) frame = burnFX.applyToFrame(frame);
        if (isFrozen())  frame = freezeFX.applyToFrame(frame);

        g2.drawImage(frame, x, y, width, height, null);
        shield.draw(g2);
        drawHealthBar(g2);
    }

    // @Override
    // protected Animation getWalkAnimation() {
    //     if (isFrozen()) {
    //         return (getSavedDx() <= 0) ? walkLeftAnimation : walkRightAnimation;
    //     }
    //     return facingLeft ? walkLeftAnimation : walkRightAnimation;
    // }

    @Override
    protected Image getImage() {
        return getWalkAnimation().getImage();
    }

    @Override
    public void respawn() {
        int panelWidth = panel.getWidth();
        x           = (facingLeft) ? panelWidth + 50 : -50;
        y           = getY();
        dx          = facingLeft ? -2 : 2;
        reachedStop = false;
        stopX       = -1;
        soundManager.playClip("appear", false);
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }

    @Override
    public void playDeathSound() {
        soundManager.playClip("die", false);
    }
}