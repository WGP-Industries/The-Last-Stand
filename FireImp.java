import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

public class FireImp extends Monster {

    private Image flyForwardLeftImage;
    private Image flyForwardRightImage;
    private Image flyBackwardLeftImage;
    private Image flyBackwardRightImage;
    private Image fireballImage;
    private ArrayList<Fireball> fireballs;

    // the imp cycles: walk to a spot → pace on the ground → rise → fly → descend →
    // repeat
    private enum Phase {
        WALKING_TO_STOP, GROUND_PACE, RISING, FLYING, DESCENDING
    }

    private Phase phase;

    private int stopX; // world X where the imp halts and starts pacing
    private int groundY; // Y to return to after descending
    private int flyY; // target Y when rising
    private int paceDirection; // +1 right, -1 left
    private int shootTimer;
    private int phaseTimer;
    private boolean impFacingLeft;

    private static final int SHOOT_INTERVAL = 50; // ticks between fireball shots
    private static final int STOP_DISTANCE = 280; // how far from the treasure the imp stops
    private static final int FLY_SPEED = 30;
    private static final int PACE_SPEED = 4;
    private static final int PACE_RANGE = 60; // half-width of the pacing corridor
    private static final int PHASE_DURATION = 100; // ticks spent on ground or in the air

    public FireImp(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 25);

        width = 50;
        height = 55;

        dx = (xPos < 0) ? 5 : -5;
        dy = 0;
        hp = 60;
        maxHp = hp;

        groundY = WorldConfig.FLOOR_Y - height;

        paceDirection = (xPos < 0) ? 1 : -1;
        shootTimer = 0;
        phaseTimer = 0;
        phase = Phase.WALKING_TO_STOP;
        stopX = -1; // calculated on first move once we know the treasure position
        impFacingLeft = (xPos >= 0);

        fireballImage = ImageManager.loadImage("images/fire_imp/fireball.png");
        fireballs = new ArrayList<>();

        walkLeftAnimation = new Animation();
        walkRightAnimation = new Animation();
        for (int i = 1; i <= 4; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_" + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_" + i + ".png"),
                    100);
        }
        walkLeftAnimation.start();
        walkRightAnimation.start();

        deathLeftAnimation = new Animation(false);
        deathRightAnimation = new Animation(false);
        for (int i = 1; i <= 11; i++) {
            deathLeftAnimation.addFrame(ImageManager.loadImage("images/fire_imp/dead/fire_imp_left_dead_" + i + ".png"),
                    100);
            deathRightAnimation
                    .addFrame(ImageManager.loadImage("images/fire_imp/dead/fire_imp_right_dead_" + i + ".png"), 100);
        }

        // fly images are just reused walk frames — forward = frame 1, backward = frame
        // 4
        flyForwardLeftImage = ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_1.png");
        flyForwardRightImage = ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_1.png");
        flyBackwardLeftImage = ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_4.png");
        flyBackwardRightImage = ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_4.png");
    }

    // called once on the first move tick so stopX is relative to the treasure's
    // actual position
    private void initStopX() {
        if (stopX == -1) {
            flyY = panel.getHeight() / 2 - 20;
            int tx = (int) treasure.getBoundingRectangle().getX();
            stopX = (dx > 0) ? tx - STOP_DISTANCE : tx + STOP_DISTANCE;
        }
    }

    private Animation getCurrentWalkAnimation() {
        if (isFrozen()) {
            // while frozen dx is 0, so check the saved direction instead
            return (getSavedDx() <= 0) ? walkLeftAnimation : walkRightAnimation;
        }
        return impFacingLeft ? walkLeftAnimation : walkRightAnimation;
    }

    private Image getCurrentFlyImage() {
        // pick forward/backward sprite based on whether pace direction matches facing
        if (impFacingLeft) {
            return (paceDirection < 0) ? flyForwardLeftImage : flyBackwardLeftImage;
        } else {
            return (paceDirection > 0) ? flyForwardRightImage : flyBackwardRightImage;
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (dying)
            return;
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dying = true;
            facingLeft = impFacingLeft; // make sure the parent draws the right death anim
            deathLeftAnimation.start();
            deathRightAnimation.start();
            playDeathSound();
        }
    }

    @Override
    public void move() {
        if (!panel.isVisible())
            return;

        applyStatusEffects();

        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) {
                anim.update();
                if (!anim.isStillActive())
                    readyToRemove = true;
            } else {
                readyToRemove = true;
            }
            return;
        }

        initStopX();

        switch (phase) {

            case WALKING_TO_STOP:
                x += dx;
                impFacingLeft = (dx < 0);
                getCurrentWalkAnimation().update();
                applyGravityAndPlatforms();

                if ((dx > 0 && x >= stopX) || (dx < 0 && x <= stopX)) {
                    x = stopX;
                    dx = paceDirection * PACE_SPEED;
                    phase = Phase.GROUND_PACE;
                    phaseTimer = 0;
                }
                break;

            case GROUND_PACE:
                x += dx;
                impFacingLeft = (dx < 0);
                getCurrentWalkAnimation().update();
                applyGravityAndPlatforms();

                // bounce at the edges of the pacing corridor
                if (x <= stopX - PACE_RANGE) {
                    dx = PACE_SPEED;
                    paceDirection = 1;
                }
                if (x >= stopX + PACE_RANGE) {
                    dx = -PACE_SPEED;
                    paceDirection = -1;
                }

                shootTimer++;
                if (shootTimer >= SHOOT_INTERVAL) {
                    shootTimer = 0;
                    shootAtTreasure();
                }

                phaseTimer++;
                if (phaseTimer >= PHASE_DURATION) {
                    phaseTimer = 0;
                    phase = Phase.RISING;
                    dy = -FLY_SPEED;
                }

                groundY = y; // keep groundY updated so descend lands at the right spot
                break;

            case RISING:
                x += dx;
                y += dy;
                impFacingLeft = (dx < 0);
                getCurrentWalkAnimation().update();

                if (x <= stopX - PACE_RANGE) {
                    dx = PACE_SPEED;
                    paceDirection = 1;
                }
                if (x >= stopX + PACE_RANGE) {
                    dx = -PACE_SPEED;
                    paceDirection = -1;
                }

                if (y <= flyY) {
                    y = flyY;
                    dy = 0;
                    phase = Phase.FLYING;
                    phaseTimer = 0;
                }
                break;

            case FLYING:
                x += dx;
                impFacingLeft = (dx < 0);

                if (x <= stopX - PACE_RANGE) {
                    dx = PACE_SPEED;
                    paceDirection = 1;
                }
                if (x >= stopX + PACE_RANGE) {
                    dx = -PACE_SPEED;
                    paceDirection = -1;
                }

                shootTimer++;
                if (shootTimer >= SHOOT_INTERVAL) {
                    shootTimer = 0;
                    shootAtTreasure();
                }

                phaseTimer++;
                if (phaseTimer >= PHASE_DURATION) {
                    phaseTimer = 0;
                    phase = Phase.DESCENDING;
                    dy = FLY_SPEED;
                }
                break;

            case DESCENDING:
                x += dx;
                y += dy;
                impFacingLeft = (dx < 0);

                if (x <= stopX - PACE_RANGE) {
                    dx = PACE_SPEED;
                    paceDirection = 1;
                }
                if (x >= stopX + PACE_RANGE) {
                    dx = -PACE_SPEED;
                    paceDirection = -1;
                }

                applyGravityAndPlatforms();
                if (y >= groundY) {
                    y = groundY;
                    dy = 0;
                    dx = paceDirection * PACE_SPEED;
                    phase = Phase.GROUND_PACE;
                    phaseTimer = 0;
                }
                break;
        }

        // tick and cull fireballs each frame
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fb = fireballs.get(i);
            fb.move();
            if (!fb.isActive())
                fireballs.remove(i);
        }

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
                getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
        }
    }

    private void shootAtTreasure() {
        int startX = x + width / 2;
        int startY = y + height / 2;
        int targetX = (int) treasure.getBoundingRectangle().getCenterX();
        int targetY = (int) treasure.getBoundingRectangle().getCenterY();

        double angle = Math.atan2(targetY - startY, targetX - startX);
        double speed = 6;

        fireballs.add(new Fireball(startX, startY, fireballImage, treasure,
                Math.cos(angle) * speed, Math.sin(angle) * speed));
    }

    @Override
    public void respawn() {
        int panelWidth = panel.getWidth();
        x = (paceDirection < 0) ? panelWidth + 50 : -50;
        y = groundY;
        dx = (x < 0) ? 5 : -5;
        dy = 0;
        paceDirection = (x < 0) ? 1 : -1;
        impFacingLeft = (x >= 0);
        stopX = -1; // recalculate on next move so it tracks the treasure again
        phase = Phase.WALKING_TO_STOP;
        phaseTimer = 0;
        shootTimer = 0;
        soundManager.playClip("appear", false);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null)
                g2.drawImage(anim.getImage(), x, y, width, height, null);
            return;
        }

        // airborne phases use a static fly sprite; everything else uses the walk
        // animation
        Image raw;
        if (phase == Phase.FLYING || phase == Phase.RISING || phase == Phase.DESCENDING) {
            raw = getCurrentFlyImage();
        } else {
            raw = getCurrentWalkAnimation().getImage();
        }

        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = frame.createGraphics();
        fg.drawImage(raw, 0, 0, width, height, null);
        fg.dispose();

        if (isBurning())
            frame = burnFX.applyToFrame(frame);
        if (isFrozen())
            frame = freezeFX.applyToFrame(frame);

        g2.drawImage(frame, x, y, width, height, null);

        for (Fireball fb : fireballs)
            fb.draw(g2);

        drawHealthBar(g2);
    }

    @Override
    public boolean isImmuneToFire() {
        return true;
    }

    @Override
    public void playDeathSound() {
        soundManager.playClip("die", false);
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }
}