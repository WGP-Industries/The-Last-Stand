public class FreezeFX extends MonsterTintFX {

    public FreezeFX() {
        super(80);
    }

    @Override
    protected int applyTint(int pixel) {
        int alpha = (pixel >> 24) & 255;
        int red   = (pixel >> 16) & 255;
        int green = (pixel >>  8) & 255;
        int blue  =  pixel        & 255;

        blue = truncateValue(blue + tintStrength);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
