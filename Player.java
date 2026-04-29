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
    private static final float MAX_SPEED_X = 14f;
    private static final float ACCELERATION = 1.5f;
    private static final float DECELERATION = 2.0f;

    // Vertical / jump physics
    private float velocityY = 0f;
    private static final float GRAVITY = 0.8f;
    private static final float JUMP_STRENGTH = -14f;
    private static final float MAX_FALL = 18f;

    private static final int GROUND_Y = WorldConfig.FLOOR_Y - 50;

    private boolean isOnGround = false;

    private Color backgroundColour;

    // Animations
    private enum FacingDirection {
        LEFT, RIGHT
    }

    private FacingDirection facing = FacingDirection.RIGHT;

    private Animation walkRightAnimation;
    private Animation walkLeftAnimation;
    private Animation jumpRightAnimation;
    private Animation jumpLeftAnimation;
    private Animation fallRightAnimation;
    private Animation fallLeftAnimation;
    private Animation idleRightAnimation;
    private Animation idleLeftAnimation;

    private Animation currentAnimation;

    // Frame timing in ms
    private static final long WALK_FRAME_MS = 80;
    private static final long JUMP_FRAME_MS = 70;
    private static final long FALL_FRAME_MS = 70;

    private BulletType currentBulletType = BulletType.BASIC;
    private float damageMultiplier = 1.0f;

    // Player can land on platforms as well as shoot through them
    private SolidObjectManager solidObjectManager = null;

    public Player(JPanel p, int xPos, int yPos) {
        panel = p;
        backgroundColour = panel.getBackground();
        x = xPos;
        y = GROUND_Y;
        width = 50;
        height = 50;

        loadAnimations();
        setAnimation(idleRightAnimation);
    }

    // Animation loading

    private void loadAnimations() {
        walkRightAnimation = new Animation(true);
        walkLeftAnimation = new Animation(true);
        for (int i = 1; i <= 8; i++) {
            walkRightAnimation.addFrame(
                    ImageManager.loadImage("images/player/walking/right/player_walking_right_" + i + ".png"),
                    WALK_FRAME_MS);
            walkLeftAnimation.addFrame(
                    ImageManager.loadImage("images/player/walking/left/player_walking_left_" + i + ".png"),
                    WALK_FRAME_MS);
        }

        jumpRightAnimation = new Animation(false);
        jumpLeftAnimation = new Animation(false);
        for (int i = 1; i <= 4; i++) {
            jumpRightAnimation.addFrame(
                    ImageManager.loadImage("images/player/jumping/right/player_jumping_right_" + i + ".png"),
                    JUMP_FRAME_MS);
            jumpLeftAnimation.addFrame(
                    ImageManager.loadImage("images/player/jumping/left/player_jumping_left_" + i + ".png"),
                    JUMP_FRAME_MS);
        }

        fallRightAnimation = new Animation(true);
        fallLeftAnimation = new Animation(true);
        for (int i = 1; i <= 4; i++) {
            fallRightAnimation.addFrame(
                    ImageManager.loadImage("images/player/falling/right/player_falling_right_" + i + ".png"),
                    FALL_FRAME_MS);
            fallLeftAnimation.addFrame(
                    ImageManager.loadImage("images/player/falling/left/player_falling_left_" + i + ".png"),
                    FALL_FRAME_MS);
        }

        // Single frame idle reuses the first walk frame as a still
        idleRightAnimation = new Animation(true);
        idleRightAnimation.addFrame(ImageManager.loadImage("images/player/walking/right/player_walking_right_1.png"),
                1000);

        idleLeftAnimation = new Animation(true);
        idleLeftAnimation.addFrame(ImageManager.loadImage("images/player/walking/left/player_walking_left_1.png"),
                1000);
    }

    private void setAnimation(Animation next) {
        if (next == currentAnimation)
            return;
        currentAnimation = next;
        currentAnimation.start();
    }

    // Animation state machine

    private void updateAnimation() {
        boolean moving = movingLeft ^ movingRight;

        if (!isOnGround) {
            if (velocityY < 0) {
                setAnimation(facing == FacingDirection.RIGHT ? jumpRightAnimation : jumpLeftAnimation);
            } else {
                setAnimation(facing == FacingDirection.RIGHT ? fallRightAnimation : fallLeftAnimation);
            }
        } else if (moving) {
            setAnimation(facing == FacingDirection.RIGHT ? walkRightAnimation : walkLeftAnimation);
        } else {
            setAnimation(facing == FacingDirection.RIGHT ? idleRightAnimation : idleLeftAnimation);
        }

        currentAnimation.update();
    }

    // Physics

    public void setSolidObjectManager(SolidObjectManager mgr) {
        solidObjectManager = mgr;
    }

    public void setMovingLeft(boolean held) {
        movingLeft = held;
    }

    public void setMovingRight(boolean held) {
        movingRight = held;
    }

    public void move(int direction) {
        if (direction == 1)
            setMovingLeft(true);
        else if (direction == 2)
            setMovingRight(true);
    }

    public void updatePhysics() {
        updateHorizontal();
        updateVertical();
        updateAnimation();
    }

    private void updateHorizontal() {
        if (movingLeft && !movingRight) {
            velocityX = Math.max(velocityX - ACCELERATION, -MAX_SPEED_X);
            facing = FacingDirection.LEFT;
        } else if (movingRight && !movingLeft) {
            velocityX = Math.min(velocityX + ACCELERATION, MAX_SPEED_X);
            facing = FacingDirection.RIGHT;
        } else {
            if (velocityX > 0)
                velocityX = Math.max(velocityX - DECELERATION, 0f);
            else if (velocityX < 0)
                velocityX = Math.min(velocityX + DECELERATION, 0f);
        }

        x += (int) velocityX;

        if (x < 0) {
            x = 0;
            velocityX = 0;
        }
        if (x > WorldConfig.WORLD_W - width) {
            x = WorldConfig.WORLD_W - width;
            velocityX = 0;
        }
    }

    private void updateVertical() {
        if (!isOnGround) {
            velocityY += GRAVITY;
            if (velocityY > MAX_FALL)
                velocityY = MAX_FALL;
            y += (int) velocityY;

            int groundY = (solidObjectManager != null)
                    ? solidObjectManager.getLandingY(x, width, y, height)
                    : GROUND_Y;

            if (y >= groundY) {
                y = groundY;
                velocityY = 0f;
                isOnGround = true;
            }
        } else {
            int groundY = (solidObjectManager != null)
                    ? solidObjectManager.getLandingY(x, width, y, height)
                    : GROUND_Y;
            if (y < groundY) {
                isOnGround = false;
            }
        }
    }

    public boolean isFalling() {
        return !isOnGround && velocityY > 0;
    }

    public void jump() {
        if (isOnGround) {
            velocityY = JUMP_STRENGTH;
            isOnGround = false;
            currentAnimation = null;
            setAnimation(facing == FacingDirection.RIGHT ? jumpRightAnimation : jumpLeftAnimation);
        }
    }

    public void bounce() {
        velocityY = JUMP_STRENGTH * 0.7f;
        isOnGround = false;
    }

    public void push(int amount) {
        x += (x < WorldConfig.WORLD_W / 2) ? -amount : amount;
        x = Math.max(0, Math.min(x, WorldConfig.WORLD_W - width));
    }

    // Shooting

    public Bullet shoot(int worldMouseX, int worldMouseY) {
        boolean shootingLeft = worldMouseX < x + width / 2;

        facing = shootingLeft ? FacingDirection.LEFT : FacingDirection.RIGHT;

        int spawnX = shootingLeft ? x - width : x + width;
        int spawnY = y;

        double dirX = worldMouseX - spawnX;
        double dirY = worldMouseY - spawnY;
        double len = Math.hypot(dirX, dirY);
        if (len == 0)
            len = 1;
        dirX /= len;
        dirY /= len;

        Bullet bullet = createBullet(spawnX, spawnY);
        bullet.damage = Math.max(1, Math.round(bullet.damage * damageMultiplier));
        bullet.setVelocity(dirX * bullet.getSpeed(), dirY * bullet.getSpeed());
        return bullet;
    }

    private Bullet createBullet(int bx, int by) {
        return switch (currentBulletType) {
            case FIRE -> new FireBullet(panel, bx, by);
            case FREEZE -> new FreezeBullet(panel, bx, by);
            case ELECTRIC -> new ElectricBullet(panel, bx, by);
            case SPIRIT -> new SpiritBullet(panel, bx, by);
            case RAPID -> new RapidBullet(panel, bx, by);
            case PIERCING -> new PiercingBullet(panel, bx, by);
            case EXPLOSIVE -> new ExplosiveBullet(panel, bx, by);
            case TELEPORT -> new TeleportBullet(panel, bx, by);
            default -> new BasicBullet(panel, bx, by);
        };
    }

    public int getCurrentCooldown() {
        return switch (currentBulletType) {
            case RAPID -> 80;
            case EXPLOSIVE -> 1200;
            case FREEZE -> 400;
            case FIRE -> 350;
            case PIERCING -> 400;
            case ELECTRIC -> 300;
            case SPIRIT -> 300;
            case TELEPORT -> 350;
            default -> 250;
        };
    }

    public void applyDamageBuff(float mult) {
        damageMultiplier = Math.min(damageMultiplier * mult, 8.0f);
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setBulletType(BulletType type) {
        currentBulletType = type;
    }

    public BulletType getCurrentBulletType() {
        return currentBulletType;
    }

    // Rendering

    public void draw(Graphics2D g2) {
        Image frame = currentAnimation.getImage();
        if (frame != null) {
            g2.drawImage(frame, x, y, width, height, null);
        }
    }

    public void erase() {
        Graphics g = panel.getGraphics();
        if (g == null)
            return;
        ((Graphics2D) g).setColor(backgroundColour);
        ((Graphics2D) g).fill(new Rectangle2D.Double(x, y, width, height));
        g.dispose();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}