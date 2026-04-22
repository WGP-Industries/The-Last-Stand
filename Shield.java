import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Shield {

    private static final int SHIELD_WIDTH  = 30;
    private static final int SHIELD_HEIGHT = 60;

    private Image shieldImage;
    private int x, y;
    private boolean facingLeft;

    public Shield() {
        shieldImage = ImageManager.loadImage("images/shield_guardian/shield.png");
    }

    public void update(int guardianX, int guardianY, int guardianWidth, boolean facingLeft) {
        this.facingLeft = facingLeft;
        y = guardianY;
        if (facingLeft) {
            x = guardianX - SHIELD_WIDTH + 15;
        } else {
            x = guardianX + guardianWidth - 15;
        }
    }

    public boolean blocks(Bullet bullet) {
        if (bullet.bypassesShield()) return false;
        return getBoundingRectangle().intersects(bullet.getBoundingRectangle());
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, SHIELD_WIDTH, SHIELD_HEIGHT);
    }

        public void draw(Graphics2D g2) {
            if (facingLeft) {
                g2.drawImage(shieldImage, x, y, SHIELD_WIDTH, SHIELD_HEIGHT, null);
            } else {
                g2.drawImage(shieldImage, x + SHIELD_WIDTH, y, -SHIELD_WIDTH, SHIELD_HEIGHT, null);
            }
        }
}