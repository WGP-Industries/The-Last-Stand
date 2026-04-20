import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class MonsterTintFX {

    protected int tintStrength;

    public MonsterTintFX(int tintStrength) {
        this.tintStrength = tintStrength;
    }

    private int truncate(int value) {
        if (value > 255) return 255;
        if (value < 0)   return 0;
        return value;
    }

    protected abstract int applyTint(int pixel);

    protected int truncateValue(int value) {
        return truncate(value);
    }

    public void draw(Graphics2D g2, BufferedImage source, int x, int y, int width, int height) {
        BufferedImage copy = ImageManager.copyImage(source);

        int w = copy.getWidth();
        int h = copy.getHeight();
        int[] pixels = new int[w * h];
        copy.getRGB(0, 0, w, h, pixels, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = applyTint(pixels[i]);
        }

        copy.setRGB(0, 0, w, h, pixels, 0, w);
        g2.drawImage(copy, x, y, width, height, null);
    }
}
