import java.awt.*;

public class Portal {

    private final int cx;
    private final int cy;

    private static final int W = 52, H = 52;

    private Animation animation;

    public Portal(int platformX, int platformY, int platformW, boolean ground) {
        cx = platformX + platformW / 2;
        cy = ground ? platformY : platformY - 18;

        animation = new Animation(true);
        for (int i = 1; i <= 8; i++) {
            animation.addFrame(ImageManager.loadImage("images/portal/portal_" + i + ".png"), 80);
        }
        animation.start();
    }

    public int getSpawnX() {
        return cx - 20;
    }

    public int getSpawnY() {
        return cy - 44;
    }

    public void update() {
        animation.update();
    }

    public void draw(Graphics2D g) {
        Image frame = animation.getImage();
        if (frame != null) {
            g.drawImage(frame, cx - W / 2, cy - H / 2, W, H, null);
        }
    }
}