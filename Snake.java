// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import javax.swing.JPanel;

// Snake class represents a snake monster
public class Snake extends Monster {
    
    public Snake(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 10);
        width = 140;
        height = 35;
        dx = (xPos < 0) ? 3 : -3;
        dy = 0;
        hp = 100;
        monsterImageRight = ImageManager.loadImage("snake.png");
        monsterImageLeft = ImageManager.loadImage("snake.png");

    }

         
    @Override
    public void playDeathSound() {
        soundManager.playClip("die2", false);
    }


    @Override
    protected void collideWithPlayer() {
        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            soundManager.playClip("hit", false);
            respawn();
        }
    }

}