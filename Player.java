// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

// Player class represents the player character in the game, allowing movement and shooting bullets.
// I used the Bat as a base here but made significant changes to fit the player character and its mechanics.
public class Player {

    private JPanel panel;
    private int x;
    private int y;
    private int width;
    private int height;

    private int dx;
    private int dy;

    private Rectangle2D.Double player;

    private Color backgroundColour;

    //private SoundManager soundManager;
    private Image playerImage;
    private Image playerLeftImage;
    private Image playerRightImage;

    // Constructor initializes the player with its position, size, images, and sound manager.
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

    // Draws the player on the panel using the current image.
    public void draw() {
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;

        g2.drawImage(playerImage, x, y, width, height, null);

        g.dispose();
    }

    // Erases the player by drawing a rectangle of the background color over its current position.
    public void erase() {
        Graphics g = panel.getGraphics();
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(backgroundColour);
        g2.fill(new Rectangle2D.Double(x, y, width, height));

        g.dispose();
    }


    // Moves the player left or right based on the direction input, ensuring it stays within panel bounds.
    public void move(int direction) {

        if (!panel.isVisible())
            return;

        int panelWidth = panel.getWidth();

        if (direction == 1) { // going left
            x = x - dx;
            playerImage = playerLeftImage;
            if (x < 0)
                x = 0;
        } else if (direction == 2) { // going right
            x = x + dx;
            playerImage = playerRightImage;
            if (x > panelWidth - width)
                x = panelWidth - width;
        }
    }

    // create bulllet infront the player gun 
    public Bullet shoot(int direction) {
      
        int bulletX = x + (width / 2) + 10;
        int bulletY = y + (height / 2) - 10;


        return new Bullet(panel, bulletX, bulletY, direction);
    }



    // Returns the bounding rectangle of the player, used for collision detection with monsters and bullets.
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }

}