import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public abstract class AnimatedBullet extends Bullet {

    protected Animation animation;

    public AnimatedBullet(JPanel panel, int xPos, int yPos,
                          String spriteFile, int frameCount, long frameDuration) {
        super(panel, xPos, yPos);

        Image strip = ImageManager.loadImage(spriteFile);
        int fw = strip.getWidth(null) / frameCount;
        int fh = strip.getHeight(null);
        width  = fw;
        height = fh;

        animation = new Animation(true);
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.drawImage(strip,
                    0, 0, fw, fh,
                    i * fw, 0, (i + 1) * fw, fh,
                    null);
            g.dispose();
            animation.addFrame(frame, frameDuration);
        }
        animation.start();
    }

    public AnimatedBullet(JPanel panel, int xPos, int yPos,
                          String spriteFile, int cols, int rows, long frameDuration) {
        super(panel, xPos, yPos);

        Image strip = ImageManager.loadImage(spriteFile);
        int fw = strip.getWidth(null) / cols;
        int fh = strip.getHeight(null) / rows;
        width  = fw;
        height = fh;

        animation = new Animation(true);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage frame = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = frame.createGraphics();
                g.drawImage(strip,
                        0, 0, fw, fh,
                        col * fw, row * fh, (col + 1) * fw, (row + 1) * fh,
                        null);
                g.dispose();
                animation.addFrame(frame, frameDuration);
            }
        }
        animation.start();
    }

    @Override
    public void move() {
        super.move();
        animation.update();
    }

    @Override
    public void draw(Graphics2D g2) {
        Image frame = animation.getImage();
        if (frame == null) return;

        AffineTransform original = g2.getTransform();
        double angle = Math.atan2(vy, vx);
        double cx = x + width  / 2.0;
        double cy = y + height / 2.0;

        g2.translate(cx, cy);
        g2.rotate(angle);
        g2.translate(-cx, -cy);
        g2.drawImage(frame, (int) x, (int) y, width, height, null);
        g2.setTransform(original);
    }







}