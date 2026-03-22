// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import javax.swing.JPanel;

public class Ghost extends Monster {

        public Ghost(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
            super(p, xPos, yPos, player, treasure, 20);
            
            width = 45;
            height = 55;
            
            dx = (xPos < 0) ? 6 : -6;
            
            dy = 0;
            hp = 50;
            
            monsterImageLeft = ImageManager.loadImage("ghost.png");
            monsterImageRight = ImageManager.loadImage("ghost.png");
        }
        
        @Override
        public void playDeathSound() {
            soundManager.playClip("die", false);
        }



        @Override
        protected void collideWithPlayer() {}

}