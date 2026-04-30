import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ShadowWalker extends Monster {

    private DisappearFX disappearFX;
    private boolean permanentlyRevealed = false;

    public ShadowWalker(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 30);

        width = 45;
        height = 55;

        dx = (xPos < 0) ? 4 : -4;
        dy = 0;
        hp = 75;
        maxHp = hp;

        walkLeftAnimation = new Animation(true);
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_1.png"), 100);
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_2.png"), 100);
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_3.png"), 100);
        walkLeftAnimation.start();

        walkRightAnimation = new Animation(true);
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_1.png"), 100);
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_2.png"), 100);
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_3.png"), 100);
        walkRightAnimation.start();

        disappearFX = new DisappearFX(xPos, yPos, width, height, getCurrentAnimation());
        disappearFX.setFullyInvisible(); // start invisible
    }

    private Animation getCurrentAnimation() {
        return (dx <= 0) ? walkLeftAnimation : walkRightAnimation;
    }

    @Override
    public void move() {
        disappearFX.setPosition(x, y);
        disappearFX.setAnimation(getCurrentAnimation());

        if (isElectrocuted()) {
            permanentlyRevealed = true;
            disappearFX.reset();
        }

        if (!permanentlyRevealed) {
            disappearFX.update();
        }

        getCurrentAnimation().update();
        super.move();
    }

    @Override
    public void draw(Graphics2D g2) {

        if (dying) {
            super.draw(g2);
            return;
        }

        Animation walkAnim = getWalkAnimation();
        Image raw = (walkAnim != null) ? walkAnim.getImage() : getImage();

        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = frame.createGraphics();
        fg.drawImage(raw, 0, 0, width, height, null);
        fg.dispose();

        if (isBurning()) {
            frame = burnFX.applyToFrame(frame);
            if (!permanentlyRevealed)
                disappearFX.reset();
        }
        if (isFrozen()) {
            frame = freezeFX.applyToFrame(frame);
            if (!permanentlyRevealed)
                disappearFX.reset();
        }
        if (isElectrocuted()) {
            disappearFX.reset();
        }

        disappearFX.drawFrame(g2, frame);
        if (disappearFX.getAlpha() > 0) {
            drawHealthBar(g2);
        }

        if (electricFX.isActive())
            electricFX.draw(g2);

    }

    @Override
    public void collideWithTreasure() {
        if (treasure != null && !treasure.isDestroyed() &&
                getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            treasure.takeDamage(damage);
            soundManager.playClip("hit", false);
            if (!permanentlyRevealed)
                disappearFX.setFullyInvisible();
            respawn();
        }
    }

    @Override
    protected Image getImage() {
        return getCurrentAnimation().getImage();
    }

    @Override
    public void playDeathSound() {
        soundManager.playClip("shadow_walker_die", false);
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            if (!permanentlyRevealed)
                disappearFX.setFullyInvisible();
            respawn();
        }
    }
}