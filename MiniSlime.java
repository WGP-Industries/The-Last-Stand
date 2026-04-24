import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class MiniSlime extends SplitSlime {

    private final BurnFX burnFX = new BurnFX();
    private final FreezeFX freezeFX = new FreezeFX();

    public MiniSlime(JPanel p, int xPos, int yPos, Player player, Treasure treasure, int parentDx) {
        super(p, xPos, yPos, player, treasure);

        width = 40;
        height = 35;
        hp = 25;
        maxHp = hp;
        dx = parentDx;
        facingLeft = (dx < 0);
        phase = Phase.WALKING;
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        if (phase == Phase.WALKING) {
            return new Rectangle2D.Double(x, y, width, height);
        }
        return new Rectangle2D.Double(-9999, -9999, 1, 1);
    }

    @Override
    public void takeDamage(int damage) {
        if (phase != Phase.WALKING) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            phase = Phase.DYING;
            dying = true;
            if (deathLeftAnimation != null) deathLeftAnimation.start();
            if (deathRightAnimation != null) deathRightAnimation.start();
            playDeathSound();
        }
    }

    @Override
    public void move() {
        if (!panel.isVisible()) return;

        applyStatusEffects();

        switch (phase) {
            case WALKING:
                x += dx;
                if (dx != 0) facingLeft = (dx < 0);
                if (!isFrozen()) getWalkAnimation().update();

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
                Animation deathAnim = getDeathAnimation();
                if (deathAnim != null) {
                    deathAnim.update();
                    if (!deathAnim.isStillActive()) {
                        phase = Phase.DEAD;
                        readyToRemove = true;
                    }
                } else {
                    phase = Phase.DEAD;
                    readyToRemove = true;
                }
                break;

            case DEAD:
                break;

            default:
                break;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        switch (phase) {
            case WALKING:
                g2.drawImage(getWalkAnimation().getImage(), x, y, width, height, null);
               
                break;
            case DYING:
                g2.drawImage(getDeathAnimation().getImage(), x, y, width, height, null);
                break;
            case DEAD:
                break;
            default:
                break;
        }

        drawHealthBar(g2);
    }

    // @Override
    // protected void drawStatusEffects(Graphics2D g2) {
    //     if (phase != Phase.WALKING) return;
    //     Image raw = getWalkAnimation().getImage();
    //     if (raw == null) return;
    //     BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    //     Graphics2D fg = frame.createGraphics();
    //     fg.drawImage(raw, 0, 0, width, height, null);
    //     fg.dispose();
    //     if (isBurning()) burnFX.draw(g2, frame, x, y, width, height);
    //     if (isFrozen()) freezeFX.draw(g2, frame, x, y, width, height);
    // }

    @Override
    public boolean isDead() {
        return phase == Phase.DEAD;
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