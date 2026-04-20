import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JPanel;

public class SplitSlime extends Monster {

    protected Animation walkLeftAnimation;
    protected Animation walkRightAnimation;
    protected Animation deathAnimation;
    protected Animation splitAnimation;

    protected enum Phase { WALKING, DYING, SPLITTING, DEAD }
    protected Phase phase;

    private ArrayList<MiniSlime> miniSlimes;
    private boolean splitPrevented = false;
    protected boolean facingLeft = true;

    private final BurnFX  burnFX  = new BurnFX();
    private final FreezeFX freezeFX = new FreezeFX();

    // Sentinel rect far offscreen ,used so a dead/splitting big slime never blocks bullets.
    private static final Rectangle2D.Double OFFSCREEN_RECT =
            new Rectangle2D.Double(-9999, -9999, 1, 1);

    public SplitSlime(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 20);

        width  = 80;
        height = 70;
        hp     = 100;
        dx     = (xPos < 0) ? 3 : -3;
        dy     = 0;
        phase  = Phase.WALKING;
        facingLeft = (dx < 0);
        miniSlimes = new ArrayList<>();

        walkLeftAnimation  = new Animation();
        walkRightAnimation = new Animation();
        deathAnimation     = new Animation(false);
        splitAnimation     = new Animation(false);

        for (int i = 1; i <= 8; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_left_walk_" + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_right_walk_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 7; i++) {
            deathAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_death_" + i + ".png"), 100);
        }

        for (int i = 1; i <= 2; i++) {
            splitAnimation.addFrame(ImageManager.loadImage("images/split_slime/split_slime_split_" + i + ".png"), 150);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    protected Animation getCurrentWalkAnimation() {
        return facingLeft ? walkLeftAnimation : walkRightAnimation;
    }

    /**
     * Returns an offscreen rectangle when the big slime is no longer walking,
     * so bullets are never blocked by the dying/splitting/dead body.
     */
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
            deathAnimation.start();
            playDeathSound();
        }
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
                deathAnimation.update();
                if (!deathAnimation.isStillActive()) {
                    if (splitPrevented) {
                        phase = Phase.DEAD;
                    } else {
                        phase = Phase.SPLITTING;
                        splitAnimation.start();
                        spawnMiniSlimes();
                    }
                }
                break;

            case SPLITTING:
                splitAnimation.update();
                if (!splitAnimation.isStillActive()) phase = Phase.DEAD;
                break;

            case DEAD:
                break;
        }
    }

    private void spawnMiniSlimes() {
        int spawnDir = (dx <= 0) ? -1 : 1;
        int miniHeight = 45;
        int spawnY = y + (height - miniHeight); // align feet to same ground level
        miniSlimes.add(new MiniSlime(panel, x,                 spawnY, player, treasure, dx));
        miniSlimes.add(new MiniSlime(panel, x + spawnDir * 40, spawnY, player, treasure, dx));
        miniSlimes.add(new MiniSlime(panel, x + spawnDir * 80, spawnY, player, treasure, dx));
    }

    public boolean hitMiniSlime(Bullet bullet) {
        if (!bullet.isActive()) return false;
        for (MiniSlime mini : miniSlimes) {
            if (!mini.isDead() &&
                    bullet.getBoundingRectangle().intersects(mini.getBoundingRectangle())) {
                bullet.onHit(mini, new java.util.ArrayList<>());
                return true;
            }
        }
        return false;
    }

    private boolean minisReleased = false;

    // Returns minis the first time they are ready to enter activeMonsters, empty list after that.
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
            case SPLITTING:
                g2.drawImage(splitAnimation.getImage(), x, y, width, height, null);
              
                break;
            case DEAD:
            
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