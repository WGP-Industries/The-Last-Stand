import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    private static int MAX_MONSTERS = 14;

    private SoundManager soundManager;
    private Player player;
    private Treasure treasure;
    private ArrayList<Bullet> bullets;
    private ArrayList<Monster> activeMonsters;
    private int monstersKilled;
    private boolean gameStarted;
    private boolean isRunning;

    private Thread gameThread;
    private BufferedImage image;

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

    public void createGameEntities() {
        player = new Player(this, getWidth() / 2, 350);
        treasure = new Treasure(getWidth() / 2, 330);

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
            int xPos = (spawnSide == 0) ? -50 : getWidth() + 50;

            int monsterType = 3;
            if (monsterType == 0) {
                activeMonsters.add(new Snake(this, xPos, 350, player, treasure));
            } else if (monsterType == 1) {
                activeMonsters.add(new Ghost(this, xPos, 350, player, treasure));
            } else if (monsterType == 2) {
                activeMonsters.add(new ArmoredTurtle(this, xPos, 350, player, treasure));
            }
            else if (monsterType == 3) {
                activeMonsters.add(new FireImp(this, xPos, 350, player, treasure));
            }
             else {
                activeMonsters.add(new ShadowWalker(this, xPos, 350, player, treasure));
            }
        }
    }

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

            if (monstersKilled >= MAX_MONSTERS) {
                isRunning = false;
                waveLabel.setText("You win at wave " + currentWave + "! ");
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

    public void gameRender() {
        Graphics2D imageContext = (Graphics2D) image.getGraphics();

        imageContext.setColor(new Color(135, 206, 235));
        imageContext.fillRect(0, 0, getWidth(), getHeight());

        imageContext.setColor(new Color(155, 118, 83));
        imageContext.fillRect(0, 390, getWidth(), getHeight() - 390);

        if (treasure != null) treasure.draw(imageContext);
        if (player != null) player.draw(imageContext);

        for (Monster monster : activeMonsters) {
            monster.draw(imageContext);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(imageContext);
        }

        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            paintChildren(g2);
            g2.dispose();
        }

        imageContext.dispose();
    }

    public void run() {
        try {
            isRunning = true;
            while (isRunning) {
                gameUpdate();
                gameRender();
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {}
    }

    public void startGame() {
        if (!gameStarted) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            isRunning = true;
            gameStarted = true;
            soundManager.playClip("background", true);
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void stopGame() {
        isRunning = false;
        if (gameThread != null) {
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