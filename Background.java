import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Background {
    private Image bgImage;
    private int drawW;      // replaces bgImageWidth everywhere
    private int drawH;

    private Dimension dimension;
    private int bgX;
    private int backgroundX;
    private int backgroundX2;
    private int bgDX;
    private float bgOffsetY = 0;
    private float bgDY;

public Background(JFrame window, String imageFile, int bgDX) {
    this.bgImage = loadImage(imageFile);
    dimension = window.getSize();

    drawH = dimension.height;
    drawW = (int)(drawH * (1920.0 / 1080.0));

    this.bgDX = bgDX;

    // initialize side by side from the start
    backgroundX  = 0;
    backgroundX2 = drawW;
}




public void moveRight() {
    backgroundX  -= bgDX;
    backgroundX2 -= bgDX;

    // when first copy fully scrolled off left, reset it to the right of second
    if (backgroundX + drawW <= 0) {
        backgroundX = backgroundX2 + drawW;
    }
    if (backgroundX2 + drawW <= 0) {
        backgroundX2 = backgroundX + drawW;
    }
}

public void moveLeft() {
    backgroundX  += bgDX;
    backgroundX2 += bgDX;

    // when first copy scrolled off right, reset it to the left of second
    if (backgroundX >= drawW) {
        backgroundX = backgroundX2 - drawW;
    }
    if (backgroundX2 >= drawW) {
        backgroundX2 = backgroundX - drawW;
    }
}
    public void draw(Graphics2D g2) {
    g2.drawImage(bgImage, (int)backgroundX,  (int)bgOffsetY, drawW, drawH, null);
    g2.drawImage(bgImage, (int)backgroundX2, (int)bgOffsetY, drawW, drawH, null);
}
    
    public void moveUp(float amount)   { bgOffsetY -= amount; }
    public void moveDown(float amount) { bgOffsetY += amount; }




    public Image loadImage(String fileName) {
        return new ImageIcon(fileName).getImage();
    }
}