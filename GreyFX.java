import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class GreyFX extends MonsterTintFX {

    public GreyFX() {
        super(0);
    }

    @Override
    protected int applyTint(int pixel) {
        int alpha = (pixel >> 24) & 255;
        int red   = (pixel >> 16) & 255;
        int green = (pixel >>  8) & 255;
        int blue  =  pixel        & 255;

        int gray  = (int)(0.2126 * red + 0.7152 * green + 0.0722 * blue);

        return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }
}
