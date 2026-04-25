import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ChainFX {

    private final Animation chainAnim;
    private boolean active = false;
    private double x1, y1, x2, y2;

    public ChainFX() {
        Image strip = ImageManager.loadImage("images/bullets/fx/chained.png");
        int frameCount = 4;
        int fw = strip.getWidth(null) / frameCount;
        int fh = strip.getHeight(null);

        chainAnim = new Animation(false);
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = frame.createGraphics();
            g.drawImage(strip, 0, 0, fw, fh, i * fw, 0, (i + 1) * fw, fh, null);
            g.dispose();
            chainAnim.addFrame(frame, 80);
        }
    }

    public void trigger(double x1, double y1, double x2, double y2) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        chainAnim.start();
        active = true;
    }

    public void tick() {
        if (active) chainAnim.update();
        if (!chainAnim.isStillActive()) active = false;
    }

    public boolean isActive() { return active; }

    public void draw(Graphics2D g2) {
        if (!active) return;
        Image frame = chainAnim.getImage();
        if (frame == null) return;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.hypot(dx, dy);
        double angle  = Math.atan2(dy, dx);

        AffineTransform old = g2.getTransform();
        g2.translate(x1, y1);
        g2.rotate(angle);
        // stretch the sprite to span the distance between monsters
        g2.drawImage(frame, 0, -8, (int) length, 16, null);
        g2.setTransform(old);
    }
}