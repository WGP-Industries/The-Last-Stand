import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

public class SplitSlime extends Monster {

    protected enum Phase { WALKING, DYING, SPLITTING, DEAD }
    protected Phase phase;

    private Animation splitAnimation;

    private ArrayList<MiniSlime> miniSlimes;
    private boolean splitPrevented = false;
    private boolean minisReleased = false;

    private final BurnFX burnFX = new BurnFX();
    private final FreezeFX freezeFX = new FreezeFX();

    private static final Rectangle2D.Double OFFSCREEN_RECT =
            new Rectangle2D.Double(-9999, -9999, 1, 1);

    public SplitSlime(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 20);

        width = 80;
        height = 70;
        hp = 100;
        maxHp = hp;
        dx = (xPos < 0) ? 3 : -3;
        dy = 0;

        phase = Phase.WALKING;
        facingLeft = (dx < 0);

        miniSlimes = new ArrayList<>();

        walkLeftAnimation = new Animation(true);
        walkRightAnimation = new Animation(true);
        deathLeftAnimation = new Animation(false);
        deathRightAnimation = new Animation(false);
        splitAnimation = new Animation(false);

        for (int i = 1; i <= 8; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_left_walk_" + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_right_walk_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 7; i++) {
            deathLeftAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_death_" + i + ".png"), 100);
            deathRightAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_death_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 2; i++) {
            splitAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_split_" + i + ".png"), 150);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        if (phase == Phase.WALKING) {
            return new Rectangle2D.Double(x, y, width, height);
        }
        return OFFSCREEN_RECT;
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


   public boolean isSplitPrevented() { return splitPrevented; }


    @Override
    public void move() {
        if (!panel.isVisible()) return;

        applyStatusEffects();

        switch (phase) {

            case WALKING:
                x += dx;
                if (dx != 0) facingLeft = (dx < 0);
                if (!isFrozen()) getWalkAnimation().update();

                if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
                    collideWithPlayer();
                    return;
                }

                if (treasure != null && !treasure.isDestroyed() &&
                        getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
                    collideWithTreasure();
                    return;
                }

                if (x < -100 || x > panel.getWidth() + 100) respawn();
                break;

            case DYING:
                Animation deathAnim = getDeathAnimation();
                if (deathAnim != null) {
                    deathAnim.update();
                    if (!deathAnim.isStillActive()) {
                        if (splitPrevented) {
                            phase = Phase.DEAD;
                            readyToRemove = true;
                        } else {
                            phase = Phase.SPLITTING;
                            splitAnimation.start();
                            spawnMiniSlimes();
                        }
                    }
                } else {
                    phase = Phase.DEAD;
                    readyToRemove = true;
                }
                break;

            case SPLITTING:
                splitAnimation.update();
                if (!splitAnimation.isStillActive()) {
                    phase = Phase.DEAD;
                    readyToRemove = true;
                }
                break;

            case DEAD:
                break;
        }
    }

    private void spawnMiniSlimes() {
        final int MINI_WIDTH = 40;
        final int GAP = 10;
        final int SPACING = MINI_WIDTH + GAP;
        final int MINI_HEIGHT = 35;

        int spawnDir = (dx < 0) ? -1 : 1;
        int spawnY = y + (height - MINI_HEIGHT);

        miniSlimes.add(new MiniSlime(panel, x - spawnDir * SPACING, spawnY, player, treasure, dx));
        miniSlimes.add(new MiniSlime(panel, x, spawnY, player, treasure, dx));
        miniSlimes.add(new MiniSlime(panel, x + spawnDir * SPACING, spawnY, player, treasure, dx));
    }

    public ArrayList<MiniSlime> drainPendingMinis() {
        if (minisReleased || miniSlimes.isEmpty()) return new ArrayList<>();
        minisReleased = true;
        return new ArrayList<>(miniSlimes);
    }

    public boolean isFullyDead() {
        if (splitPrevented) return phase == Phase.DEAD;
        for (MiniSlime mini : miniSlimes) {
            if (!mini.isDead()) return false;
        }
        return !miniSlimes.isEmpty();
    }

    public void preventSplit() { splitPrevented = true; }


    public boolean hitMiniSlime(Bullet bullet) {
    if (!bullet.isActive()) return false;

    for (MiniSlime mini : miniSlimes) {
        if (!mini.isDead() &&
            bullet.getBoundingRectangle().intersects(mini.getBoundingRectangle())) {

            bullet.onHit(mini, new ArrayList<>());
            return true;
        }
    }
    return false;
}
@Override
protected Animation getWalkAnimation() {
    if (isFrozen()) {
        return (getSavedDx() < 0) ? walkLeftAnimation : walkRightAnimation;
    }
    return facingLeft ? walkLeftAnimation : walkRightAnimation;
}

    @Override
    public void draw(Graphics2D g2) {
        switch (phase) {
            case WALKING:
                BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fg = frame.createGraphics();
                fg.drawImage(getWalkAnimation().getImage(), 0, 0, width, height, null);
                fg.dispose();

                if (isBurning()) frame = burnFX.applyToFrame(frame);
                if (isFrozen())  frame = freezeFX.applyToFrame(frame);

                g2.drawImage(frame, x, y, width, height, null);
                drawHealthBar(g2);
                break;
            case DYING:
                g2.drawImage(getDeathAnimation().getImage(), x, y, width, height, null);
                break;
            case SPLITTING:
                g2.drawImage(splitAnimation.getImage(), x, y, width, height, null);
                break;
            case DEAD:
                break;
        }
    }

    @Override
    public boolean isDead() {
        return isFullyDead();
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