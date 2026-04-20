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

    private BulletType currentBulletType = BulletType.BASIC;

    public Player(JPanel p, int xPos, int yPos) {
        panel = p;

        backgroundColour = panel.getBackground();
        x = xPos;
        y = yPos;

        dx = 10;
        dy = 0;

        width = 50;
        height = 50;

        playerLeftImage = ImageManager.loadImage("images/player_left.png");
        playerRightImage = ImageManager.loadImage("images/player_right.png");

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
            x           += dx;
            playerImage  = playerRightImage;
            if (x > panelWidth - width) x = panelWidth - width;
        }
    }

    public Bullet shoot(int mouseX, int mouseY) {
        // Spawn point: horizontal centre, vertical centre of the player sprite (whange when we make a new sprite)
        int spawnX = x + width  / 2;
        int spawnY = y + height / 2;

        double dirX = mouseX - spawnX;
        double dirY = mouseY - spawnY;

        double length = Math.hypot(dirX, dirY);
        if (length == 0) length = 1;
        dirX /= length;
        dirY /= length;

        // Flip sprite based on horizontal mouse direction
        playerImage = (mouseX < x + width / 2) ? playerLeftImage : playerRightImage;

        Bullet bullet = createBullet(spawnX, spawnY);

        bullet.setVelocity(dirX * bullet.getSpeed(), dirY * bullet.getSpeed());

        return bullet;
    }

    private Bullet createBullet(int bx, int by) {
        switch (currentBulletType) {
            case FIRE:      return new FireBullet     (panel, bx, by);
            case FREEZE:    return new FreezeBullet   (panel, bx, by);
            case ELECTRIC:  return new ElectricBullet (panel, bx, by);
            case SPIRIT:    return new SpiritBullet   (panel, bx, by);
            case RAPID:     return new RapidBullet    (panel, bx, by);
            case PIERCING:  return new PiercingBullet (panel, bx, by);
            case EXPLOSIVE: return new ExplosiveBullet(panel, bx, by);
            case TELEPORT:  return new TeleportBullet (panel, bx, by);
            default:        return new BasicBullet    (panel, bx, by);
        }
    }

    public int getCurrentCooldown() {
        switch (currentBulletType) {
            case RAPID:     return 80;
            case EXPLOSIVE: return 1200;
            case FREEZE:    return 400;
            case FIRE:      return 350;
            case PIERCING:  return 400;
            case ELECTRIC:  return 300;
            case SPIRIT:    return 300;
            case TELEPORT:  return 350;
            default:        return 250;
        }
    }

    public void setBulletType(BulletType type) { currentBulletType = type; }
    public BulletType getCurrentBulletType()   { return currentBulletType; }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}
