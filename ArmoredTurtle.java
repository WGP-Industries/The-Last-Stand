import java.awt.Image;
import javax.swing.JPanel;

public class ArmoredTurtle extends Monster {

    public ArmoredTurtle(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 40);

        width = 80;
        height = 60;

        dx = (xPos < 0) ? 2 : -2;
        dy = 0;
        hp = 60;
        maxHp = hp;
        walkLeftAnimation = new Animation(true);
        walkRightAnimation = new Animation(true);

        for (int i = 1; i <= 8; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/armored_turtle/armored_turtle_left_" + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/armored_turtle/armored_turtle_right_" + i + ".png"), 100);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    private Animation getCurrentAnimation() {
        return (dx <= 0) ? walkLeftAnimation : walkRightAnimation;
    }

    @Override
    public void move() {
        getCurrentAnimation().update();
        super.move();
    }

    @Override
    public void takeDamage(int damage) {
        hp -= damage / 2;
        if (isDead()) playDeathSound();
    }

    @Override
    public boolean isResistantToPiercing() { return false; }

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