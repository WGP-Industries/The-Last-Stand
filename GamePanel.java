import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable {

    private static int MAX_MONSTERS = 1; // max kills to win

    private SoundManager soundManager;
    private Player player;
    private Treasure treasure;
    private ArrayList<Bullet> bullets;
    private ArrayList<Monster> activeMonsters;
    private int monstersKilled; // total kills
    private boolean gameStarted; // game state
    private boolean isRunning; // loop state

    private Thread gameThread;
    private BufferedImage image;

    private JLabel scoreLabel;
    private JLabel waveLabel;
    private JLabel bulletLabel;
    private int currentWave; // wave count
    private Random random;

    private long lastShotTime = 0; // fire cooldown

    public GamePanel() {
        scoreLabel  = new JLabel("Score: 0");
        waveLabel   = new JLabel("Wave: 0");
        bulletLabel = new JLabel("Bullet: BASIC  [1-9 to switch]");

        currentWave    = 0;
        monstersKilled = 0;

        player = null;
        activeMonsters = null;

        gameStarted = false;
        isRunning = false;

        soundManager = SoundManager.getInstance();
        random = new Random();
    }

    public void createGameEntities() {
        player = new Player(this, getWidth() / 2, 350); // spawn player
        treasure = new Treasure(getWidth() / 2, 330); // spawn base

        activeMonsters = new ArrayList<>();
        bullets = new ArrayList<>();
    }

    private void spawnWave() {
        if (monstersKilled >= MAX_MONSTERS) return; // stop if done

        currentWave++;
        waveLabel.setText("Wave: " + currentWave);

        int remaining = MAX_MONSTERS - monstersKilled;
        int waveSize = Math.min(random.nextInt(4) + 1, remaining); // random count

        for (int i = 0; i < waveSize; i++) {
            int spawnSide = random.nextInt(2); // left or right
            int xPos = (spawnSide == 0) ? -50 : getWidth() + 50; // off screen

            int monsterType = 4; // temp fixed type

            if (monsterType == 0) {
                activeMonsters.add(new Snake(this, xPos, 350, player, treasure));
            } else if (monsterType == 1) {
                activeMonsters.add(new Ghost(this, xPos, 350, player, treasure));
            } else if (monsterType == 2) {
                activeMonsters.add(new ArmoredTurtle(this, xPos, 350, player, treasure));
            } else if (monsterType == 3) {
                activeMonsters.add(new FireImp(this, xPos, 350, player, treasure));
            } else if (monsterType == 4) {
                activeMonsters.add(new SplitSlime(this, xPos, 330, player, treasure));
            } else {
                activeMonsters.add(new ShadowWalker(this, xPos, 350, player, treasure));
            }
        }
    }

    public void gameUpdate() {

        if (treasure != null && treasure.isDestroyed()) {
            isRunning = false; // stop loop
            waveLabel.setText("Game over at wave: " + currentWave + "! ");
            scoreLabel.setText(" Final Score: " + monstersKilled);
            stopGame();
            return;
        }

        try {
            for (Monster monster : activeMonsters) monster.move(); // move enemies

            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet bullet = bulletIter.next();
                bullet.move(); // move bullet

                if (!bullet.isActive()) {
                    bulletIter.remove(); // remove dead bullet
                    continue;
                }

                boolean consumed = false;

                for (Monster monster : activeMonsters) {
                    if (monster.isDead()) continue;

                    if (bullet.getBoundingRectangle().intersects(monster.getBoundingRectangle())) {
                        bullet.onHit(monster, activeMonsters); // apply damage

                        if (!bullet.isPiercing()) {
                            consumed = true; // remove if not piercing
                            break;
                        }
                    }
                }

                if (consumed) bulletIter.remove();
            }

            ArrayList<Monster> toAdd = new ArrayList<>();

            for (Monster m : activeMonsters) {
                if (m instanceof SplitSlime && !(m instanceof MiniSlime)) {
                    toAdd.addAll(((SplitSlime) m).drainPendingMinis()); // spawn minis
                }
            }

            activeMonsters.addAll(toAdd);

            Iterator<Monster> monsterIter = activeMonsters.iterator();
            while (monsterIter.hasNext()) {
                Monster m = monsterIter.next();

                if (!m.isDead()) continue;

                if (!(m instanceof MiniSlime)) monstersKilled++; // count kill
                monsterIter.remove(); // remove dead
            }

            scoreLabel.setText("Score: " + monstersKilled);

            if (monstersKilled >= MAX_MONSTERS) {
                waveLabel.setText("You win at wave " + currentWave + "!");
                scoreLabel.setText("Final Score: " + monstersKilled);
                stopGame();
                return;
            }

            if (activeMonsters.isEmpty()) spawnWave(); // next wave

        } catch (Exception e) {
            System.out.println("Error during game update: " + e.getMessage());
        }
    }

    public void updatePlayer(int direction) {
        if (player != null && gameStarted) player.move(direction); // move player
    }

    public void shootBullet(int mouseX, int mouseY) {
        if (player == null || !gameStarted) return; // ignore if not ready

        long now = System.currentTimeMillis();
        int cooldown = player.getCurrentCooldown(); // get fire rate

        if (now - lastShotTime >= cooldown) {
            bullets.add(player.shoot(mouseX, mouseY)); // shoot
            soundManager.playClip("shoot", false);
            lastShotTime = now; // reset timer
        }
    }

    public void setBulletType(BulletType type) {
        if (player != null) {
            player.setBulletType(type); // change type
            bulletLabel.setText("Bullet: " + type.name());
        }
    }

    public void gameRender() {
        Graphics2D imageContext = (Graphics2D) image.getGraphics();

        imageContext.setColor(new Color(135, 206, 235)); // sky
        imageContext.fillRect(0, 0, getWidth(), getHeight());

        imageContext.setColor(new Color(155, 118, 83)); // ground
        imageContext.fillRect(0, 390, getWidth(), getHeight() - 390);

        if (treasure != null) treasure.draw(imageContext);
        if (player   != null) player.draw(imageContext);

        for (Monster monster : activeMonsters) monster.draw(imageContext);
        for (Bullet  bullet  : bullets)        bullet.draw(imageContext);

        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            paintChildren(g2); // draw ui
            g2.dispose();
        }

        imageContext.dispose();
    }

    @Override
    public void run() {
        try {
            isRunning = true;

            while (isRunning) {
                gameUpdate(); // update logic
                gameRender(); // draw frame
                Thread.sleep(50); // control speed
            }

        } catch (InterruptedException e) {}
    }

    public void startGame() {
        if (!gameStarted) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

            isRunning = true;
            gameStarted = true;

            soundManager.playClip("background", true); // start music

            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void stopGame() {
        isRunning = false;
        if (gameThread != null) soundManager.stopClip("background"); // stop music
    }

    public boolean isGameStarted()  { return gameStarted; }
    public JLabel  getScoreLabel()  { return scoreLabel;  }
    public JLabel  getWaveLabel()   { return waveLabel;   }
    public JLabel  getBulletLabel() { return bulletLabel; }
}

