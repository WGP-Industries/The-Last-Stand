import java.awt.Graphics2D;
import javax.swing.JPanel;

public class ShadowWalker extends Monster {

    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private DisappearFX disappearFX;
    private int visibilityTimer;
    private boolean isInvisible;
    private static final int INVISIBLE_DURATION = 1060;
    private static final int VISIBLE_DURATION = 40;

    public ShadowWalker(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 30);

        width = 45;
        height = 55;

        dx = (xPos < 0) ? 4 : -4;
        dy = 0;
        hp = 75;

        isInvisible = false;
        visibilityTimer = VISIBLE_DURATION;

        walkLeftAnimation = new Animation();
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_1.png"), 100);
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_2.png"), 100);
        walkLeftAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_left_3.png"), 100);
        walkLeftAnimation.start();

        walkRightAnimation = new Animation();
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_1.png"), 100);
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_2.png"), 100);
        walkRightAnimation.addFrame(ImageManager.loadImage("images/shadow_walker/shadow_walker_right_3.png"), 100);
        walkRightAnimation.start();

        disappearFX = new DisappearFX((GamePanel) p, xPos, yPos, width, height, getCurrentAnimation());
    }

    private Animation getCurrentAnimation() {
        return (dx <= 0) ? walkLeftAnimation : walkRightAnimation;
    }

    @Override
    public void move() {
        visibilityTimer--;
        
        if (visibilityTimer <= 0) {
            isInvisible = !isInvisible;
            visibilityTimer = isInvisible ? INVISIBLE_DURATION : VISIBLE_DURATION;
            if (!isInvisible) disappearFX.reset();
        }

        if (isInvisible) {
            disappearFX.setPosition(x, y);
            disappearFX.setAnimation(getCurrentAnimation());
            disappearFX.update();
        }

        getCurrentAnimation().update();
        super.move();
    }

    @Override
    public void draw(Graphics2D g2) {
        java.awt.Image frame = getCurrentAnimation().getImage();
        if (isInvisible) {
            disappearFX.draw(g2);
        } else {
            g2.drawImage(frame, x, y, width, height, null);
        }
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public void reveal() {
        isInvisible = false;
        visibilityTimer = VISIBLE_DURATION;
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