import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class MiniSlime extends SplitSlime {

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
            case WALKING -> {
                x += dx;
                if (dx != 0) facingLeft = (dx < 0);
                if (!isFrozen()) getWalkAnimation().update();

                // Gravity
                applyGravityAndPlatforms();
                tickAI();

                if (sharedMonsterList != null) collideWithMonster(sharedMonsterList);

                if (treasure != null && !treasure.isDestroyed() &&
                        getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
                    collideWithTreasure();
                    return;
                }

                if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
                    collideWithPlayer();
                    return;
                }

               if (x < -100 || x > WorldConfig.WORLD_W + 100) respawn();

            }

            case DYING -> {
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
            }

            case DEAD -> {
            }

            default -> {
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        switch (phase) {
            case WALKING -> g2.drawImage(getWalkAnimation().getImage(), x, y, width, height, null);
            case DYING -> g2.drawImage(getDeathAnimation().getImage(), x, y, width, height, null);
            case DEAD -> {
            }
            default -> {
            }
        }
        drawHealthBar(g2);
    }

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