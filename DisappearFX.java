import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DisappearFX implements ImageFX {

	private GamePanel panel;

	private int x;
	private int y;
	private int width;
	private int height;

	private Animation animation;

	int time, timeChange;
	int alpha, alphaChange;


	public DisappearFX (GamePanel p, int xPos, int yPos, int w, int h, Animation anim) {
		panel = p;

		x = xPos;
		y = yPos;
		width = w;
		height = h;

		animation = anim;

		time = 0;
		timeChange = 1;

		alpha = 255;
		alphaChange = 10;
	}


	public void setPosition(int xPos, int yPos) {
		x = xPos;
		y = yPos;
	}


	public void draw(Graphics2D g2) {
		java.awt.Image frame = animation.getImage();

		BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D copyG2 = copy.createGraphics();
		copyG2.drawImage(frame, 0, 0, width, height, null);
		copyG2.dispose();

		int[] pixels = new int[width * height];
		copy.getRGB(0, 0, width, height, pixels, 0, width);

		for (int i = 0; i < pixels.length; i++) {
			int a = (pixels[i] >> 24) & 255;
			int red = (pixels[i] >> 16) & 255;
			int green = (pixels[i] >> 8) & 255;
			int blue = pixels[i] & 255;

			if (a != 0) {
				pixels[i] = blue | (green << 8) | (red << 16) | (alpha << 24);
			}
		}

		copy.setRGB(0, 0, width, height, pixels, 0, width);

		g2.drawImage(copy, x, y, width, height, null);
	}


	public Rectangle2D.Double getBoundingRectangle() {
		return new Rectangle2D.Double(x, y, width, height);
	}


    public void setAnimation(Animation anim) {
    animation = anim;
}

public void reset() {
    alpha = 255;
}

    public void update() {
        if (alpha > 20) {
            alpha -= alphaChange;
        }
    }
}