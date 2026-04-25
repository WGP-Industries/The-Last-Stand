import java.awt.image.BufferedImage;

public class FlickerDeathFX {

    private static final int TOTAL_TICKS = 64;

    private int ticksElapsed  = 0;
    private boolean visible   = true;
    private int flickerTimer  = 0;

    public void reset() {
        ticksElapsed = 0;
        visible      = true;
        flickerTimer = 0;
    }

    public void tick() {
        ticksElapsed++;
        flickerTimer++;

        float progress        = (float) ticksElapsed / TOTAL_TICKS;
        int   flickerInterval = Math.max(2, (int)(6 * (1f - progress)));

        if (flickerTimer >= flickerInterval) {
            visible      = !visible;
            flickerTimer = 0;
        }
    }

    public boolean isFinished() {
        return ticksElapsed >= TOTAL_TICKS;
    }

    public BufferedImage applyToFrame(BufferedImage source) {
        if (!visible) return null;

        float progress = (float) ticksElapsed / TOTAL_TICKS;
        int   alpha    = (int)(255 * (1f - progress));

        BufferedImage copy = ImageManager.copyImage(source);
        int w = copy.getWidth(), h = copy.getHeight();
        int[] pixels = new int[w * h];
        copy.getRGB(0, 0, w, h, pixels, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            int origAlpha = (pixels[i] >> 24) & 0xFF;
            if (origAlpha == 0) continue;
            int blended = Math.min(origAlpha, alpha);
            pixels[i]   = (blended << 24) | (pixels[i] & 0x00FFFFFF);
        }

        copy.setRGB(0, 0, w, h, pixels, 0, w);
        return copy;
    }
}