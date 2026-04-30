import java.awt.Graphics2D;
import javax.swing.JFrame;

public class BackgroundManager {

    private Background[] backgrounds;
    private int numBackgrounds;

    // BackgroundManager manages multiple Background layers to create a parallax
    // scrolling effect.
    public BackgroundManager(JFrame window, int moveSize) {
        this(window, moveSize,
                new String[] {
                        "images/surface/sky.png",
                        "images/surface/jungle_bg.png",
                        "images/surface/trees&bushes.png",
                        "images/surface/lianas.png",
                        "images/surface/fireflies.png",
                        "images/surface/grasses.png",
                },
                new int[] { 1, 2, 2, 3, 3, 3 });
    }

    public BackgroundManager(JFrame window, int moveSize, String[] imageFiles, int[] moveAmounts) {

        numBackgrounds = imageFiles.length;
        backgrounds = new Background[numBackgrounds];
        for (int i = 0; i < numBackgrounds; i++) {
            backgrounds[i] = new Background(window, imageFiles[i], moveAmounts[i]);
        }
    }

    public void moveRight() {
        for (int i = 0; i < numBackgrounds; i++)
            backgrounds[i].moveRight();
    }

    public void moveLeft() {
        for (int i = 0; i < numBackgrounds; i++)
            backgrounds[i].moveLeft();
    }

    public void moveUp(float[] amounts) {
        for (int i = 0; i < numBackgrounds; i++)
            backgrounds[i].moveUp(amounts[i]);
    }

    public void moveDown(float[] amounts) {
        for (int i = 0; i < numBackgrounds; i++)
            backgrounds[i].moveDown(amounts[i]);
    }

    public void draw(Graphics2D g2) {
        for (int i = 0; i < numBackgrounds; i++)
            backgrounds[i].draw(g2);
    }
}