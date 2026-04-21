import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public class ShadowWalker extends Monster {

    private DisappearFX disappearFX;

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

        // Start with alpha = 0 so it's fully invisible from the beginning
        disappearFX = new DisappearFX(xPos, yPos, width, height, getCurrentAnimation());
        
    }

    private Animation getCurrentAnimation() {
        return (dx <= 0) ? walkLeftAnimation : walkRightAnimation;
    }

        @Override
        public void move() {
            disappearFX.setPosition(x, y);
            disappearFX.setAnimation(getCurrentAnimation());
            disappearFX.update(); // drives the fade back to invisible every frame

            getCurrentAnimation().update();
            super.move();
        }


    @Override
    public void draw(Graphics2D g2) {
        // Always render through disappearFX (handles full invisibility and fade states)
        disappearFX.draw(g2);
        if (disappearFX.getAlpha() > 0) {

            drawHealthBar(g2);
        }
        
    drawStatusEffects(g2);
    }

    @Override
    public void collideWithTreasure() {
        if (treasure != null && !treasure.isDestroyed() &&
                getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            treasure.takeDamage(damage);
            soundManager.playClip("hit", false);
            disappearFX.reset(); // alpha back to 255, update() handles fading automatically
            respawn();
        }
    }

    @Override
protected Image getImage() {
    return getCurrentAnimation().getImage();
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