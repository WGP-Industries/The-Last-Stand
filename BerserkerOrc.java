import javax.swing.JPanel;

public class BerserkerOrc extends Monster {

    private static final int BASE_SPEED  = 3;
    private static final int BASE_DAMAGE = 30;
   
    private static final int RAGE_SPEED  = 7;
    private static final int RAGE_DAMAGE = 60;

    public BerserkerOrc(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, BASE_DAMAGE);

        width  = 70;
        height = 80;
        hp     = 200;
        maxHp  = hp;
        dx     = (xPos < 0) ? BASE_SPEED : -BASE_SPEED;

        facingLeft = (dx < 0);

        walkLeftAnimation  = new Animation(true);
        walkRightAnimation = new Animation(true);
        deathLeftAnimation  = new Animation(false);
        deathRightAnimation = new Animation(false);

        for (int i = 1; i <= 3; i++) {
            walkLeftAnimation.addFrame(ImageManager.loadImage("images/berserker_orc/berserker_orc_left_walk_"  + i + ".png"), 100);
            walkRightAnimation.addFrame(ImageManager.loadImage("images/berserker_orc/berserker_orc_right_walk_" + i + ".png"), 100);
        }
        for (int i = 1; i <= 8; i++) {
            deathLeftAnimation.addFrame(ImageManager.loadImage("images/berserker_orc/dead/left/berserker_orc_left_dead_"   + i + ".png"), 100);
            deathRightAnimation.addFrame(ImageManager.loadImage("images/berserker_orc/dead/right/berserker_orc_right_dead_" + i + ".png"), 100);
        }

        walkLeftAnimation.start();
        walkRightAnimation.start();
    }

    private boolean isRaging() {
        return isBurning() || (float) hp / maxHp <= 0.4f;
    }

    private void updateRageStats() {
        if (isFrozen()) return;
        int dir = facingLeft ? -1 : 1;
        if (isRaging()) { dx = dir * RAGE_SPEED; damage = RAGE_DAMAGE; }
        else            { dx = dir * BASE_SPEED;  damage = BASE_DAMAGE; }
    }

    @Override
    public void applyBurn(int dmgPerStep, int steps) {
        super.applyBurn(dmgPerStep * 2, steps);
    }

    @Override
    public void takeDamage(int amount) {
        if (dying) return;
        hp -= amount;
        if (hp <= 0) {
            hp    = 0;
            dying = true;
            facingLeft = (dx <= 0);
            deathLeftAnimation.start();
            deathRightAnimation.start();
            playDeathSound();
        }
    }

    @Override
    public void move() {
        if (!panel.isVisible()) return;

        applyStatusEffects();
        updateRageStats();

        if (dying) {
            Animation anim = getDeathAnimation();
            if (anim != null) {
                anim.update();
                if (!anim.isStillActive()) readyToRemove = true;
            } else {
                readyToRemove = true;
            }
            return;
        }

        x += dx;
        if (dx != 0) facingLeft = (dx < 0);

        // Gravity
        applyGravityAndPlatforms();
        tickAI();
        getWalkAnimation().update();

        if (sharedMonsterList != null) collideWithMonster(sharedMonsterList);

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            collideWithPlayer();
            return;
        }

        if (treasure != null && !treasure.isDestroyed() &&
            getBoundingRectangle().intersects(treasure.getBoundingRectangle())) {
            collideWithTreasure();
            return;
        }

        if (x < -2000 || x > WorldConfig.WORLD_W + 2000) respawn();
    }

    @Override
    public void respawn() {
        x      = facingLeft ? panel.getWidth() + 50 : -50;
        dx     = facingLeft ? -BASE_SPEED : BASE_SPEED;
        damage = BASE_DAMAGE;
        soundManager.playClip("appear", false);
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }

    @Override
    public void playDeathSound() { soundManager.playClip("die", false); }
}
