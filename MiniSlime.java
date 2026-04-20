import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class MiniSlime extends SplitSlime {

    private final BurnFX   burnFX   = new BurnFX();
    private final FreezeFX freezeFX = new FreezeFX();

    public MiniSlime(JPanel p, int xPos, int yPos, Player player, Treasure treasure, int parentDx) {
        super(p, xPos, yPos, player, treasure);

        width  = 40;
        height = 35;
        hp     = 25;
        dx     = parentDx;
        facingLeft = (dx < 0);
        phase  = Phase.WALKING;
    }

    /**
     * Uses the mini slime's own width/height, not the parent class values.
     * Only returns a real rect while WALKING ,dead/dying mini slimes don't block bullets.
     */
    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        if (phase == Phase.WALKING) {
            return new Rectangle2D.Double(x, y, width, height);
        }
        return new Rectangle2D.Double(-9999, -9999, 1, 1);
    }

    @Override
    public void move() {
        if (!panel.isVisible()) return;

        applyStatusEffects();
        if (isDead()) return;

        switch (phase) {
            case WALKING:
                x += dx;
                if (dx != 0) facingLeft = (dx < 0);
                if (!isFrozen()) getCurrentWalkAnimation().update();

                if (treasure != null && !treasure.isDestroyed() &&
                    getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
                    collideWithTreasure();
                    return;
                }

                if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
                    collideWithPlayer();
                    return;
                }

                if (x < -100 || x > panel.getWidth() + 100) respawn();
                break;

            case DYING:
                deathAnimation.update();
                if (!deathAnimation.isStillActive()) phase = Phase.DEAD;
                break;

            case DEAD:
                break;

            default:
                break;
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (phase != Phase.WALKING) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            phase = Phase.DYING;
            deathAnimation.start();
            playDeathSound();
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        switch (phase) {
            case WALKING:
                g2.drawImage(getCurrentWalkAnimation().getImage(), x, y, width, height, null);
                drawStatusEffects(g2);
                break;
            case DYING:
                g2.drawImage(deathAnimation.getImage(), x, y, width, height, null);
                break;
            case DEAD:
                break;
            default:
                break;
        }
    }

    @Override
    protected void drawStatusEffects(Graphics2D g2) {
        if (phase != Phase.WALKING) return;
        java.awt.Image raw = getCurrentWalkAnimation().getImage();
        if (raw == null) return;
        java.awt.image.BufferedImage frame = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D fg = frame.createGraphics();
        fg.drawImage(raw, 0, 0, width, height, null);
        fg.dispose();
        if (isBurning()) burnFX.draw(g2, frame, x, y, width, height);
        if (isFrozen())  freezeFX.draw(g2, frame, x, y, width, height);
    }

    @Override
    public boolean isDead() {
        return phase == Phase.DEAD;
    }

    @Override
    public boolean hitMiniSlime(Bullet bullet) {
        return false;
    }

    @Override
    public void playDeathSound() {
        soundManager.playClip("die", false);
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }
}