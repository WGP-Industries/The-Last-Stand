// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
   A component that displays all the game entities
*/

// Honestly I think I used lab 4  more than GamePanel-Bat-Alien-Images lab 3 for A1
public class GamePanel extends JPanel {

    private static int MAX_MONSTERS = 14;

    private SoundManager soundManager;
    private Player player;
    private Treasure treasure;
    private ArrayList<Bullet> bullets;
    private ArrayList<Monster> activeMonsters;
    private int monstersKilled;
    private boolean gameStarted;
    private boolean isRunning;

    private Timer gameTimer;
    

    // I am sure you probably wanted these in gameWindow but I just felt it was easier to do this
    private JLabel scoreLabel;
    private JLabel waveLabel;
    private int currentWave;
    private Random random;

    public GamePanel() {
      


        scoreLabel = new JLabel("Score: 0");
        waveLabel = new JLabel("Wave: 0");

        currentWave = 0;
        monstersKilled = 0;

        player = null;
        activeMonsters = null;
      
        gameStarted = false;
        isRunning = false;

        soundManager = SoundManager.getInstance();
        random = new Random();
    }

    // Initializes the player and treasure, sets up monster and bullet lists, and spawns the first monster wave.
    public void createGameEntities() {
        player = new Player(this, getWidth() / 2, 350);
        treasure = new Treasure(this, getWidth() / 2, 330);

        activeMonsters = new ArrayList<>();
        bullets = new ArrayList<>();
      

    
    }

    private void spawnWave() {
        if (monstersKilled >= MAX_MONSTERS) return;

        currentWave++;
        waveLabel.setText("Wave: " + currentWave);

        int remaining = MAX_MONSTERS - monstersKilled;
        int waveSize = Math.min(random.nextInt(4) + 1, remaining);

        for (int i = 0; i < waveSize; i++) {
            int spawnSide = random.nextInt(2);
            // To make the game a bit more interesting, monsters can from both sides so. Terraria style :).
            int xPos = (spawnSide == 0) ? -50 : getWidth() + 50;


            // I love having to resubmit late because i forgot I made it only aliens could spawn : D
            // I'll take the L yes 
            if (random.nextInt(2) == 0) {
                activeMonsters.add(new Snake(this, xPos, 350, player, treasure));
            } else {
                activeMonsters.add(new Ghost(this, xPos, 350, player, treasure));
            }
        }
    }

    // Updates game state
    public void gameUpdate() {

        if (treasure != null && treasure.isDestroyed()) {
            isRunning = false;
            waveLabel.setText("Game over at wave: " + currentWave + "! ");
            scoreLabel.setText(" Final Score: " + monstersKilled);
            stopGame();
            return;
        }

        try {
            for (int i = activeMonsters.size() - 1; i >= 0; i--) {
                Monster monster = activeMonsters.get(i);
                monster.move();
            }

            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet bullet = bullets.get(i);
                bullet.move();

                if (!bullet.isActive()) {
                    bullets.remove(i);
                    continue;
                }

                for (int j = activeMonsters.size() - 1; j >= 0; j--) {
                    Monster monster = activeMonsters.get(j);

                    if (!monster.isDead()) {
                        Rectangle2D.Double bulletRect = bullet.getBoundingRectangle();
                        Rectangle2D.Double monsterRect = monster.getBoundingRectangle();

                        if (bulletRect.intersects(monsterRect)) {
                            monster.takeDamage(bullet.getDamage());
                            bullets.remove(i);

                            if (monster.isDead()) {
                                activeMonsters.remove(j);
                                monstersKilled++;
                                scoreLabel.setText("Score: " + monstersKilled);
                            }
                            break;
                        }
                    }
                }
            }

             if(monstersKilled >= MAX_MONSTERS) {
                isRunning = false;
                waveLabel.setText("You win at wave " + currentWave +  "! ");
                scoreLabel.setText("  With a final Score: " + monstersKilled);
                stopGame();
                return;
            }

            if (activeMonsters.isEmpty() && monstersKilled < MAX_MONSTERS) {
                spawnWave();
            }

        } catch (Exception e) {
            System.out.println("Error during game update: " + e.getMessage());
        }
    }

    public void updatePlayer(int direction) {
        if (player != null) {
            player.move(direction);
        }
    }

    public void shootBullet(int direction) {
        if (player != null) {

            bullets.add(player.shoot(direction));
            soundManager.playClip("shoot", false);
        }
    }

    // Rendering
    public void gameRender() {
        Graphics g = getGraphics();
        if (g == null) return;

        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(155, 118, 83));  
        g.fillRect(0, 390, getWidth(), getHeight() - 390);

        if (treasure != null) treasure.draw();
        if (player != null) player.draw();

        for (Monster monster : activeMonsters) {
            monster.draw();
        }

        for (Bullet bullet : bullets) {
            bullet.draw();
        }

        paintChildren(g);
        g.dispose();
    }

    // Start game using Swing Timer, threading got me weird results so I just went with this.
    public void startGame() {
        if (!gameStarted) {

            isRunning = true;

            gameTimer = new Timer(50, e -> {
                if (isRunning) {
                    gameUpdate();
                    gameRender();
                }
            });

            gameTimer.start();
            soundManager.playClip("background", true);
            gameStarted = true;
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void stopGame() {
        isRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
                  soundManager.stopClip("background");

        }
    }  
    
    
    


    public JLabel getScoreLabel() {
        return scoreLabel;
    }

    public JLabel getWaveLabel() {
        return waveLabel;
    }


}