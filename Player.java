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

    // Horizontal movement
    private float velocityX = 0f;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private static final float MAX_SPEED_X = 8f;
    private static final float ACCELERATION = 1.5f;
    private static final float DECELERATION = 2.0f;

    // Vertical / jump physics
    private float velocityY = 0f;
    private static final float GRAVITY = 0.8f;
    private static final float JUMP_STRENGTH = -14f;
    private static final float MAX_FALL = 18f;
    private static final int   GROUND_Y = 340;
    private boolean isOnGround = false;



    private Color backgroundColour;

    private Image playerImage;
    private Image playerLeftImage;
    private Image playerRightImage;

    private BulletType currentBulletType = BulletType.BASIC;

    public Player(JPanel p, int xPos, int yPos) {
        panel = p;

        backgroundColour = panel.getBackground();
        x = xPos;
        y = GROUND_Y;

        width  = 50;
        height = 50;

        playerLeftImage = ImageManager.loadImage("images/player_left.png");
        playerRightImage = ImageManager.loadImage("images/player_right.png");

        playerImage = playerRightImage;
    }

    public boolean isFalling() {
        return !isOnGround && velocityY > 0;
    }

    public void bounce() {
        velocityY  = JUMP_STRENGTH * 0.7f;   // 70% of a full jump so it feels lighter
        isOnGround = false;
    }

    public void jump() {
        if (isOnGround) {
            velocityY  = JUMP_STRENGTH;
            isOnGround = false;
        }
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

    public void setMovingLeft(boolean held)  { movingLeft  = held; }
    public void setMovingRight(boolean held) { movingRight = held; }

    public void updatePhysics() {
        updateHorizontal();
        updateVertical();
    }

    private void updateHorizontal() {
        int panelWidth = panel.getWidth();

        if (movingLeft && !movingRight) {
            velocityX = Math.max(velocityX - ACCELERATION, -MAX_SPEED_X);
            playerImage = playerLeftImage;
        } else if (movingRight && !movingLeft) {
            velocityX = Math.min(velocityX + ACCELERATION, MAX_SPEED_X);
            playerImage = playerRightImage;
        } else {
            if (velocityX > 0) velocityX = Math.max(velocityX - DECELERATION, 0f);
            else if (velocityX < 0) velocityX = Math.min(velocityX + DECELERATION, 0f);
        }

        x += (int) velocityX;
        if (x < 0)                    { x = 0;                   velocityX = 0; }
        if (x > panelWidth - width)   { x = panelWidth - width;  velocityX = 0; }
    }

    private void updateVertical() {
        if (!isOnGround) {
            velocityY += GRAVITY;
            if (velocityY > MAX_FALL) velocityY = MAX_FALL;
            y += (int) velocityY;
            if (y >= GROUND_Y) {
                y          = GROUND_Y;
                velocityY  = 0f;
                isOnGround = true;
            }
        }
    }

    public void move(int direction) {
        if (direction == 1) setMovingLeft(true);
        else if (direction == 2) setMovingRight(true);
    }

        public void push(int amount) {
        x += (x < panel.getWidth() / 2) ? -amount : amount;
        x = Math.max(0, Math.min(x, panel.getWidth() - width));
    }
 

public Bullet shoot(int mouseX, int mouseY) {

    boolean shootingLeft = mouseX < x + width / 2;

    // Spawn point depends on direction
    int spawnX = shootingLeft ? x - width : x + width;
    int spawnY = y;

    double dirX = mouseX - spawnX;
    double dirY = mouseY - spawnY;

    double length = Math.hypot(dirX, dirY);
    if (length == 0) length = 1;
    dirX /= length;
    dirY /= length;

    // Flip sprite
    playerImage = shootingLeft ? playerLeftImage : playerRightImage;

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
