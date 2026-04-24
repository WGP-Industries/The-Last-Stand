import javax.swing.JPanel;

public class Snake extends Monster {

    public Snake(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 10);

        width = 140;
        height = 35;
        dx = (xPos < 0) ? 3 : -3;
        dy = 0;

        hp = 100;
        maxHp = hp;

        monsterImageRight = ImageManager.loadImage("images/snake/snake_right.png");
        monsterImageLeft = ImageManager.loadImage("images/snake/snake_left.png");
    }


    @Override
    public void playDeathSound() {
        soundManager.playClip("die2", false);
    }

    @Override
    public void takeDamage(int damage) {
        
        if (isFrozen()) {
            damage = 100;
        }
        super.takeDamage(damage );
    }

    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }
}