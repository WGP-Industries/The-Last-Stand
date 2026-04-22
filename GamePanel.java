import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    private static int MAX_MONSTERS = 1;

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
    private JLabel bulletLabel;
    private int currentWave;
    private Random random;

    private long lastShotTime = 0;

    public GamePanel() {
        scoreLabel  = new JLabel("Score: 0");
        waveLabel   = new JLabel("Wave: 0");
        bulletLabel = new JLabel("Bullet: BASIC  [1-9 to switch]");

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
        player = new Player(this, getWidth() / 2, 250);
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
            int baseX = (spawnSide == 0) ? -50 : getWidth() + 50;

            int xPos = (spawnSide == 0)
                    ? baseX - random.nextInt(200)
                    : baseX + random.nextInt(200);

            int monsterType = 7;

            Monster newMonster;

            if (monsterType == 0) {
                newMonster = new Snake(this, xPos, 350, player, treasure);
            } else if (monsterType == 1) {
                newMonster = new Ghost(this, xPos, 350, player, treasure);
            } else if (monsterType == 2) {
                newMonster = new ArmoredTurtle(this, xPos, 350, player, treasure);
            } else if (monsterType == 3) {
                newMonster = new FireImp(this, xPos, 350, player, treasure);
            } else if (monsterType == 4) {
                newMonster = new SplitSlime(this, xPos, 330, player, treasure);
            } else if (monsterType == 5) {
                newMonster = new Healer(this, xPos, 350, player, treasure);
            } else if (monsterType == 6) {
                newMonster = new ShieldGuardian(this, xPos, 330, player, treasure);
             } else if (monsterType == 7 ){
                  newMonster = new BerserkerOrc(this, xPos, 350, player, treasure);
             }
            
            else {
                newMonster = new ShadowWalker(this, xPos, 350, player, treasure);
            }

            boolean collision;
            int safety = 0;

            do {
                collision = false;

                Rectangle2D.Double newRect = newMonster.getBoundingRectangle();

                for (Monster m : activeMonsters) {
                    if (newRect.intersects(m.getBoundingRectangle())) {

                        if (spawnSide == 0) newMonster.x -= 50;
                        else newMonster.x += 50;

                        collision = true;
                        newRect = newMonster.getBoundingRectangle();
                    }
                }

                safety++;

            } while (collision && safety < 10);

            activeMonsters.add(newMonster);
        }
    }

    public ArrayList<Monster> getMonsters() {
        return activeMonsters;
    }

    public void gameUpdate() {

        if (treasure != null && treasure.isDestroyed()) {
            isRunning = false;
            waveLabel.setText("Game over at wave: " + currentWave + "!");
            scoreLabel.setText("Final Score: " + monstersKilled);
            stopGame();
            return;
        }

        try {

            for (Monster monster : activeMonsters) {
                if (monster instanceof Healer healer) {
                    healer.move(activeMonsters);
                } else {
                    monster.move();
                }
            }

            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet bullet = bulletIter.next();
                bullet.move();

                if (!bullet.isActive()) {
                    bulletIter.remove();
                    continue;
                }

                boolean consumed = false;

for (Monster monster : activeMonsters) {

    if (monster instanceof SplitSlime slime) {
        if (slime.hitMiniSlime(bullet)) {
            if (!bullet.isPiercing()) {
                consumed = true;
                break;
            }
        }
    }

    if (monster instanceof ShieldGuardian sg) {
    if (sg.getShield().blocks(bullet)) {
            consumed = true;
            break;
    }
}

    if (monster.isDead()) continue;

    if (bullet.getBoundingRectangle().intersects(monster.getBoundingRectangle())) {
        bullet.onHit(monster, activeMonsters);

        if (!bullet.isPiercing()) {
            consumed = true;
            break;
        }
    }
}






                if (consumed) bulletIter.remove();
            }

            ArrayList<Monster> toAdd = new ArrayList<>();

            for (Monster m : activeMonsters) {
                if (m instanceof SplitSlime && !(m instanceof MiniSlime)) {
                    toAdd.addAll(((SplitSlime) m).drainPendingMinis());
                }
            }

            activeMonsters.addAll(toAdd);

            Iterator<Monster> monsterIter = activeMonsters.iterator();
            while (monsterIter.hasNext()) {
                Monster m = monsterIter.next();

                if (!m.isReadyToRemove()) continue;

                if (m instanceof SplitSlime slime) {
                    if (slime.isFullyDead()) {
                        monstersKilled++;
                        monsterIter.remove();
                    }
                } else if (!(m instanceof MiniSlime)) {
                    monstersKilled++;
                    monsterIter.remove();
                } else {
                    monsterIter.remove();
                }
            }

            scoreLabel.setText("Score: " + monstersKilled);

            if (monstersKilled >= MAX_MONSTERS) {
                waveLabel.setText("You win at wave " + currentWave + "!");
                scoreLabel.setText("Final Score: " + monstersKilled);
                stopGame();
                return;
            }

            if (activeMonsters.isEmpty()) spawnWave();

        } catch (Exception e) {
            System.out.println("Error during game update: " + e.getMessage());
        }
    }

    public void updatePlayer(int direction) {
        if (player != null && gameStarted) player.move(direction);
    }

    public void shootBullet(int mouseX, int mouseY) {
        if (player == null || !gameStarted) return;

        long now = System.currentTimeMillis();
        int cooldown = player.getCurrentCooldown();

        if (now - lastShotTime >= cooldown) {
            bullets.add(player.shoot(mouseX, mouseY));
            soundManager.playClip("shoot", false);
            lastShotTime = now;
        }
    }

    public void setBulletType(BulletType type) {
        if (player != null) {
            player.setBulletType(type);
            bulletLabel.setText("Bullet: " + type.name());
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

        for (Monster monster : activeMonsters) monster.draw(imageContext);
        for (Bullet bullet : bullets) bullet.draw(imageContext);

        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            paintChildren(g2);
            g2.dispose();
        }

        imageContext.dispose();
    }

    @Override
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

    public void stopGame() {
        isRunning = false;
        gameStarted = false;
        if (gameThread != null) soundManager.stopClip("background");
    }

    public boolean isGameStarted() { return gameStarted; }
    public JLabel getScoreLabel() { return scoreLabel; }
    public JLabel getWaveLabel() { return waveLabel; }
    public JLabel getBulletLabel() { return bulletLabel; }
}