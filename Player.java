import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class Player {

    private JPanel panel;
    private int x;
    private int y;
    private int width;
    private int height;

    private int dx;
    private int dy;



    private Color backgroundColour;

    private Image playerImage;
    private Image playerLeftImage;
    private Image playerRightImage;

    public Player(JPanel p, int xPos, int yPos) {
        panel = p;

        backgroundColour = panel.getBackground();
        x = xPos;
        y = yPos;

        dx = 10;
        dy = 0;

        width = 50;
        height = 50;

        playerLeftImage = ImageManager.loadImage("player_left.png");
        playerRightImage = ImageManager.loadImage("player_right.png");

        playerImage = playerRightImage;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(playerImage, x, y, width, height, null);
    }

    public void erase() {
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(backgroundColour);
        g2.fill(new Rectangle2D.Double(x, y, width, height));

        g.dispose();
    }

    public void move(int direction) {
        if (!panel.isVisible())
            return;

        int panelWidth = panel.getWidth();

        if (direction == 1) {
            x = x - dx;
            playerImage = playerLeftImage;
            if (x < 0)
                x = 0;
        } else if (direction == 2) {
            x = x + dx;
            playerImage = playerRightImage;
            if (x > panelWidth - width)
                x = panelWidth - width;
        }
    }

    public Bullet shoot(int direction) {
        int bulletX = x + (width / 2) + 10;
        int bulletY = y + (height / 2) - 10;
        return new Bullet(panel, bulletX, bulletY, direction);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}