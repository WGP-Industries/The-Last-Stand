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
    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private static final float MAX_SPEED_X   = 14f;
    private static final float ACCELERATION  = 1.5f;
    private static final float DECELERATION  = 2.0f;

    // Vertical / jump physics
    private float velocityY    = 0f;
    private static final float GRAVITY      = 0.8f;
    private static final float JUMP_STRENGTH = -14f;
    private static final float MAX_FALL      = 18f;

    private static final int GROUND_Y = WorldConfig.FLOOR_Y - 50;

    private boolean isOnGround = false;

    private Color backgroundColour;
    private Image playerImage;
    private Image playerLeftImage;
    private Image playerRightImage;

    private BulletType currentBulletType = BulletType.BASIC;
    private float damageMultiplier = 1.0f;

    // Player can land on platform, aswell as shoot through them
    private SolidObjectManager solidObjectManager = null;

    public Player(JPanel p, int xPos, int yPos) {
        panel = p;
        backgroundColour = panel.getBackground();
        x      = xPos;
        y      = GROUND_Y;
        width  = 50;
        height = 50;
        playerLeftImage  = ImageManager.loadImage("images/player_left.png");
        playerRightImage = ImageManager.loadImage("images/player_right.png");
        playerImage      = playerRightImage;
    }

    public void setSolidObjectManager(SolidObjectManager mgr) {
        solidObjectManager = mgr;
    }

    public void setMovingLeft (boolean held) { movingLeft  = held; }
    public void setMovingRight(boolean held) { movingRight = held; }

    public void move(int direction) {
        if (direction == 1) setMovingLeft(true);
        else if (direction == 2) setMovingRight(true);
    }

    public void updatePhysics() {
        updateHorizontal();
        updateVertical();
    }

    private void updateHorizontal() {
        if (movingLeft && !movingRight) {
            velocityX = Math.max(velocityX - ACCELERATION, -MAX_SPEED_X);
            playerImage = playerLeftImage;
        } else if (movingRight && !movingLeft) {
            velocityX = Math.min(velocityX + ACCELERATION,  MAX_SPEED_X);
            playerImage = playerRightImage;
        } else {
            if      (velocityX > 0) velocityX = Math.max(velocityX - DECELERATION, 0f);
            else if (velocityX < 0) velocityX = Math.min(velocityX + DECELERATION, 0f);
        }

        x += (int) velocityX;

        if (x < 0) { x = 0; velocityX = 0; }
        if (x > WorldConfig.WORLD_W - width)    { x = WorldConfig.WORLD_W - width;  velocityX = 0; }
    }

    private void updateVertical() {
        if (!isOnGround) {
            velocityY += GRAVITY;
            if (velocityY > MAX_FALL) velocityY = MAX_FALL;
            y += (int) velocityY;

            // Find the effective floor (platform surface or the world floor)
            int groundY = (solidObjectManager != null)
                    ? solidObjectManager.getLandingY(x, width, y, height)
                    : GROUND_Y;

            if (y >= groundY) {
                y          = groundY;
                velocityY  = 0f;
                isOnGround = true;
            }
        } else {
            // Stay grounded — if the platform walked off from under us, start falling.
            int groundY = (solidObjectManager != null)
                    ? solidObjectManager.getLandingY(x, width, y, height)
                    : GROUND_Y;
            if (y < groundY) {
                isOnGround = false;   // walked off platform edge
            }
        }
    }

    public boolean isFalling() { return !isOnGround && velocityY > 0; }

    public void jump() {
        if (isOnGround) { velocityY = JUMP_STRENGTH; isOnGround = false; }
    }

    public void bounce() {
        velocityY  = JUMP_STRENGTH * 0.7f;
        isOnGround = false;
    }

    public void push(int amount) {
        x += (x < WorldConfig.WORLD_W / 2) ? -amount : amount;
        x  = Math.max(0, Math.min(x, WorldConfig.WORLD_W - width));
    }

    public Bullet shoot(int worldMouseX, int worldMouseY) {
        boolean shootingLeft = worldMouseX < x + width / 2;
        int spawnX = shootingLeft ? x - width : x + width;
        int spawnY = y;

        double dirX = worldMouseX - spawnX;
        double dirY = worldMouseY - spawnY;
        double len  = Math.hypot(dirX, dirY);
        if (len == 0) len = 1;
        dirX /= len;
        dirY /= len;

        playerImage = shootingLeft ? playerLeftImage : playerRightImage;

        Bullet bullet = createBullet(spawnX, spawnY);
        bullet.damage = Math.max(1, Math.round(bullet.damage * damageMultiplier));
        bullet.setVelocity(dirX * bullet.getSpeed(), dirY * bullet.getSpeed());
        return bullet;
    }

    private Bullet createBullet(int bx, int by) {
        return switch (currentBulletType) {
            case FIRE      -> new FireBullet     (panel, bx, by);
            case FREEZE    -> new FreezeBullet   (panel, bx, by);
            case ELECTRIC  -> new ElectricBullet (panel, bx, by);
            case SPIRIT    -> new SpiritBullet   (panel, bx, by);
            case RAPID     -> new RapidBullet    (panel, bx, by);
            case PIERCING  -> new PiercingBullet (panel, bx, by);
            case EXPLOSIVE -> new ExplosiveBullet(panel, bx, by);
            case TELEPORT  -> new TeleportBullet (panel, bx, by);
            default        -> new BasicBullet    (panel, bx, by);
        };
    }

    public int getCurrentCooldown() {
        return switch (currentBulletType) {
            case RAPID     -> 80;
            case EXPLOSIVE -> 1200;
            case FREEZE    -> 400;
            case FIRE      -> 350;
            case PIERCING  -> 400;
            case ELECTRIC  -> 300;
            case SPIRIT    -> 300;
            case TELEPORT  -> 350;
            default        -> 250;
        };
    }

    public void applyDamageBuff(float mult) {
        damageMultiplier = Math.min(damageMultiplier * mult, 8.0f);
    }

    public float getDamageMultiplier() { return damageMultiplier; }

    public void       setBulletType(BulletType type) { currentBulletType = type; }
    public BulletType getCurrentBulletType()          { return currentBulletType; }

    public void draw(Graphics2D g2) {
        g2.drawImage(playerImage, x, y, width, height, null);
    }

    public void erase() {
        Graphics g = panel.getGraphics();
        if (g == null) return;
        ((Graphics2D) g).setColor(backgroundColour);
        ((Graphics2D) g).fill(new Rectangle2D.Double(x, y, width, height));
        g.dispose();
    }

    public int getX()      { return x; }
    public int getY()      { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}
