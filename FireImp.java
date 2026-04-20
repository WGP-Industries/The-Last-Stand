import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.JPanel;

public class FireImp extends Monster {

    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Image flyForwardLeftImage;
    private Image flyForwardRightImage;
    private Image flyBackwardLeftImage;
    private Image flyBackwardRightImage;
    private Image fireballImage;
    private ArrayList<Fireball> fireballs;

    private enum Phase { WALKING_TO_STOP, GROUND_PACE, RISING, FLYING, DESCENDING }
    private Phase phase;

    private int stopX;
    private int groundY;
    private int flyY;
    private int paceDirection;
    private int shootTimer;
    private int phaseTimer;

    private static final int SHOOT_INTERVAL = 50;
    private static final int STOP_DISTANCE  = 280;
    private static final int FLY_SPEED      = 30;
    private static final int PACE_SPEED     = 4;
    private static final int PACE_RANGE     = 60;
    private static final int PHASE_DURATION = 100;

    public FireImp(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 25);

        width  = 50;
        height = 55;

        dx = (xPos < 0) ? 5 : -5;
        dy = 0;
        hp = 60;

        groundY      = yPos;
        paceDirection = (xPos < 0) ? 1 : -1;
        shootTimer   = 0;
        phaseTimer   = 0;
        phase        = Phase.WALKING_TO_STOP;

        // stopX calculated lazily in move() once panel is sized
        stopX = -1;

        fireballImage = ImageManager.loadImage("images/fire_imp/fireball.png");
        fireballs     = new ArrayList<>();

        walkLeftAnimation  = new Animation();
        walkRightAnimation = new Animation();
        for (int i = 1; i <= 4; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_" + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_" + i + ".png"), 100);
        }
        walkLeftAnimation.start();
        walkRightAnimation.start();

        flyForwardLeftImage   = ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_1.png");
        flyForwardRightImage  = ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_1.png");
        flyBackwardLeftImage  = ImageManager.loadImage("images/fire_imp/fire_imp_left_walk_4.png");
        flyBackwardRightImage = ImageManager.loadImage("images/fire_imp/fire_imp_right_walk_4.png");
    }

    private void initStopX() {
        if (stopX == -1) {
            flyY  = panel.getHeight() / 2 - 20;
            int tx = (int) treasure.getBoundingRectangle().getX();
            stopX = (dx > 0) ? tx - STOP_DISTANCE : tx + STOP_DISTANCE;
        }
    }

    private Animation getCurrentWalkAnimation() {
        return (dx <= 0) ? walkLeftAnimation : walkRightAnimation;
    }

    private Image getCurrentFlyImage() {
        if (paceDirection < 0) {
            return (dx < 0) ? flyForwardLeftImage : flyBackwardLeftImage;
        } else {
            return (dx > 0) ? flyForwardRightImage : flyBackwardRightImage;
        }
    }

    @Override
    public void move() {
        if (!panel.isVisible()) return;

        initStopX();

        switch (phase) {

            case WALKING_TO_STOP:
                x += dx;
                getCurrentWalkAnimation().update();

                if ((dx > 0 && x >= stopX) || (dx < 0 && x <= stopX)) {
                    x     = stopX;
                    dx    = paceDirection * PACE_SPEED;
                    phase = Phase.GROUND_PACE;
                    phaseTimer = 0;
                }
                break;

            case GROUND_PACE:
                x += dx;
                getCurrentWalkAnimation().update();

                if (x <= stopX - PACE_RANGE) { dx = PACE_SPEED;  paceDirection =  1; }
                if (x >= stopX + PACE_RANGE) { dx = -PACE_SPEED; paceDirection = -1; }

                shootTimer++;
                if (shootTimer >= SHOOT_INTERVAL) { shootTimer = 0; shootAtTreasure(); }

                phaseTimer++;
                if (phaseTimer >= PHASE_DURATION) {
                    phaseTimer = 0;
                    phase = Phase.RISING;
                    dy = -FLY_SPEED;
                }
                break;

            case RISING:
                x += dx;
                y += dy;

                getCurrentWalkAnimation().update();

                if (x <= stopX - PACE_RANGE) { dx = PACE_SPEED;  paceDirection =  1; }
                if (x >= stopX + PACE_RANGE) { dx = -PACE_SPEED; paceDirection = -1; }

                if (y <= flyY) {
                    y  = flyY;
                    dy = 0;
                    phase = Phase.FLYING;
                    phaseTimer = 0;
                }
                break;

            case FLYING:
                x += dx;

                if (x <= stopX - PACE_RANGE) { dx = PACE_SPEED;  paceDirection =  1; }
                if (x >= stopX + PACE_RANGE) { dx = -PACE_SPEED; paceDirection = -1; }

                shootTimer++;
                if (shootTimer >= SHOOT_INTERVAL) { shootTimer = 0; shootAtTreasure(); }

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

                if (x <= stopX - PACE_RANGE) { dx = PACE_SPEED;  paceDirection =  1; }
                if (x >= stopX + PACE_RANGE) { dx = -PACE_SPEED; paceDirection = -1; }

                if (y >= groundY) {
                    y  = groundY;
                    dy = 0;
                    dx = paceDirection * PACE_SPEED;
                    phase = Phase.GROUND_PACE;
                    phaseTimer = 0;
                }
                break;
        }

        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fb = fireballs.get(i);
            fb.move();
            if (!fb.isActive()) fireballs.remove(i);
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
        int startX  = x + width / 2;
        int startY  = y + height / 2;
        int targetX = (int) treasure.getBoundingRectangle().getCenterX();
        int targetY = (int) treasure.getBoundingRectangle().getCenterY();

        double angle = Math.atan2(targetY - startY, targetX - startX);
        double speed = 6;

        fireballs.add(new Fireball(panel, startX, startY, fireballImage, treasure,
                Math.cos(angle) * speed, Math.sin(angle) * speed));
    }

    @Override
    public void respawn() {
        int panelWidth = panel.getWidth();
        x  = (paceDirection < 0) ? panelWidth + 50 : -50;
        y  = groundY;
        dx = (x < 0) ? 5 : -5;
        dy = 0;
        paceDirection = (x < 0) ? 1 : -1;
        stopX      = -1;
        phase      = Phase.WALKING_TO_STOP;
        phaseTimer = 0;
        shootTimer = 0;
        soundManager.playClip("appear", false);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (phase == Phase.FLYING || phase == Phase.RISING || phase == Phase.DESCENDING) {
            g2.drawImage(getCurrentFlyImage(), x, y, width, height, null);
        } else {
            g2.drawImage(getCurrentWalkAnimation().getImage(), x, y, width, height, null);
        }

        for (Fireball fb : fireballs) {
            fb.draw(g2);
        }
    }

    public boolean isImmuneToFire() { return true; }

    @Override
    public void playDeathSound() { soundManager.playClip("die", false); }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }
}