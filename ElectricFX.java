import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ElectricFX {

    private final Animation shockedAnim;
    private boolean active = false;
    private int x, y, width, height;
 
    public ElectricFX() {
        Image strip = ImageManager.loadImage("images/bullets/fx/shocked.png");
        int frameCount = 8;
        int fw = strip.getWidth(null) / frameCount;
        int fh = strip.getHeight(null);

        shockedAnim = new Animation(false); // play once
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = frame.createGraphics();
            g.drawImage(strip, 0, 0, fw, fh, i * fw, 0, (i + 1) * fw, fh, null);
            g.dispose();
            shockedAnim.addFrame(frame, 80);
        }
    }

    public void trigger(int monsterX, int monsterY, int monsterW, int monsterH) {
        this.x = monsterX;
        this.y = monsterY - 10;
        this.width  = monsterW;
        this.height = monsterH;
        shockedAnim.start();
        active = true;
    }




    

    public void tick() {
    if (!active) return;
    shockedAnim.update();
    if (!shockedAnim.isStillActive()) active = false;
}


public void updatePosition(int monsterX, int monsterY, int monsterW, int monsterH) {
    this.x      = monsterX;
    this.y      = monsterY - 10;
    this.width  = monsterW;
    this.height = monsterH;
}





    public boolean isActive() { return active; }

    public void draw(Graphics2D g2) {
        if (!active) return;
        Image frame = shockedAnim.getImage();
        if (frame != null) g2.drawImage(frame, x, y, width, height, null);
    }
}