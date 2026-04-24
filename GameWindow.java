import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.*;


public class GameWindow extends JFrame
        implements Runnable, KeyListener, MouseListener, MouseMotionListener {


    private enum GameState { MENU, PLAYING, PAUSED, GAME_OVER, WIN,LEVEL_COMPLETE  }
    private volatile GameState state = GameState.MENU;

    private static final int W = 800;
    private static final int H = 600;

    private JPanel gameArea;

    private BufferedImage offscreen;
    private Thread        gameThread;
    private volatile boolean isRunning;

    private final SoundManager soundManager = SoundManager.getInstance();
    private Random random = new Random();

    // Game Variables

    private static final int MAX_MONSTERS  = 10;
    private static final int STOMP_DAMAGE  = 40;   // HP removed from a monster per stomp

    private Player player;
    private Treasure treasure;
    private ArrayList<Bullet> bullets;
    private ArrayList<Monster> activeMonsters;

    private int monstersKilled;
    private int currentWave;
    private long lastShotTime;
    private int completedLevel = 0;
    private boolean gameStarted;

    private JLabel scoreLabel = new JLabel("Score: 0");
    private JLabel waveLabel = new JLabel("Wave: 0");
    private JLabel bulletLabel = new JLabel("Bullet: BASIC  [1-9 to switch]");


    private static final String SAVE_FILE = "save.dat";


    private final WaveManager waveManager = new WaveManager();
    private int mouseX = W / 2;
    private int mouseY = H / 2;

    // On-screen Buttons

    private static final Rectangle BTN_MENU_START = new Rectangle(300, 260, 200, 50);
    private static final Rectangle BTN_MENU_EXIT = new Rectangle(300, 330, 200, 50);

    private static final Rectangle BTN_PAUSE_RESUME = new Rectangle(300, 210, 200, 50);
    private static final Rectangle BTN_PAUSE_MENU = new Rectangle(300, 280, 200, 50);
    private static final Rectangle BTN_PAUSE_EXIT = new Rectangle(300, 350, 200, 50);

    private static final Rectangle BTN_END_MENU = new Rectangle(300, 300, 200, 50);
    private static final Rectangle BTN_END_EXIT = new Rectangle(300, 370, 200, 50);


    private static final Rectangle BTN_LEVEL_CONTINUE = new Rectangle(220, 360, 160, 45);
    private static final Rectangle BTN_LEVEL_MENU     = new Rectangle(420, 360, 160, 45);
    
    private static final Rectangle BTN_MENU_CONTINUE = new Rectangle(300, 195, 200, 50);
    





    // hover states for buttons
    private boolean hMenuStart, hMenuExit;
    private boolean hPauseResume, hPauseMenu, hPauseExit;
    private boolean hEndMenu, hEndExit;
    private boolean hLevelContinue, hLevelMenu;
    private boolean hMenuContinue;



    public GameWindow() {
        super("Last Stand");

        gameArea = new JPanel();
        gameArea.setIgnoreRepaint(true);
        gameArea.setPreferredSize(new Dimension(W, H));

        setContentPane(gameArea);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);

        addKeyListener(this);
        gameArea.addMouseListener(this);
        gameArea.addMouseMotionListener(this);

        setFocusable(true);
        setVisible(true);
        requestFocusInWindow();

        offscreen   = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        isRunning   = true;
        gameStarted = false;
        gameThread  = new Thread(this);
        gameThread.start();

        createGameEntities();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                if (state == GameState.PLAYING) gameUpdate();
                gameRender();
                Thread.sleep(50);
            }
        } catch (InterruptedException ignored) {}
    }

    public void createGameEntities() {
        player = new Player(gameArea, W / 2, 250);
        treasure = new Treasure(W / 2, 330);
        activeMonsters = new ArrayList<>();
        bullets = new ArrayList<>();
        waveManager.reset();
        monstersKilled = 0;
        currentWave = 0;
        lastShotTime = 0;
        scoreLabel.setText("Score: 0");
        waveLabel.setText("Wave: 0");
    }

    public void startGame() {
        if (!gameStarted) {
            createGameEntities();
            spawnWave();
            state = GameState.PLAYING;
            gameStarted = true;
            soundManager.playClip("background", true);
        }
    }

    public void stopGame() {
        state = GameState.MENU;
        gameStarted = false;
        soundManager.stopClip("background");
    }

    public void updatePlayer(int direction) {
        if (player != null && gameStarted) player.move(direction);
    }

    public void shootBullet(int mouseX, int mouseY) {
        if (player == null || !gameStarted) return;
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= player.getCurrentCooldown()) {
            bullets.add(player.shoot(mouseX, mouseY));
            soundManager.playClip("shoot", false);
            lastShotTime = now;
        }
    }

public void setBulletType(BulletType type) {
    if (player == null) return;
    if (!waveManager.getUnlockedBullets().contains(type)) {
        bulletLabel.setText("Bullet: " + type.name() + " (locked!)  [1-9 to switch]");
        return;
    }
    player.setBulletType(type);
    bulletLabel.setText("Bullet: " + type.name() + "  [1-9 to switch]");
}

    // Update
    public void gameUpdate() {

        if (treasure != null && treasure.isDestroyed()) {
            waveLabel.setText("Game over at wave: " + currentWave + "!");
            scoreLabel.setText("Final Score: " + monstersKilled);
            endGame(false);
            return;
        }

        // Apply jump / gravity physics every tick
        if (player != null) {
            player.updatePhysics();
            checkStompCollisions();
        }

        try {
            // Move monsters
            for (Monster m : activeMonsters) {
                if (m instanceof Healer h) h.move(activeMonsters);
                else                        m.move();
            }

            // Move bullets and resolve collisions
            Iterator<Bullet> bi = bullets.iterator();
            while (bi.hasNext()) {
                Bullet b = bi.next();
                b.move();
                if (!b.isActive()) { bi.remove(); continue; }

                boolean consumed = false;
                for (Monster m : activeMonsters) {

                    if (m instanceof SplitSlime ss && ss.hitMiniSlime(b)) {
                        if (!b.isPiercing()) { consumed = true; break; }
                    }

                    if (m instanceof ShieldGuardian sg && sg.getShield().blocks(b)) {
                        consumed = true; break;
                    }

                    if (m.isDead()) continue;

                    if (b.getBoundingRectangle().intersects(m.getBoundingRectangle())) {
                        b.onHit(m, activeMonsters);
                        if (!b.isPiercing()) { consumed = true; break; }
                    }
                }
                if (consumed) bi.remove();
            }

            // Drain pending mini-slimes from SplitSlimes
            ArrayList<Monster> toAdd = new ArrayList<>();
            for (Monster m : activeMonsters) {
                if (m instanceof SplitSlime ss && !(m instanceof MiniSlime)) {
                    ArrayList<MiniSlime> minis = ss.drainPendingMinis();
                    for (MiniSlime mini : minis) mini.sharedMonsterList = activeMonsters;
                    toAdd.addAll(minis);
                }
            }
            activeMonsters.addAll(toAdd);

            // Remove dead monsters and tally score
            Iterator<Monster> mi = activeMonsters.iterator();
           while (mi.hasNext()) {
            Monster m = mi.next();

            if (m instanceof MiniSlime mini) {
                if (mini.isReadyToRemove()) mi.remove();
                continue;
            }

            if (m instanceof SplitSlime ss) {
                if (ss.isReadyToRemove() && ss.isFullyDead()) {
                    monstersKilled++;
                    mi.remove();
                } else if (ss.isReadyToRemove() && ss.isSplitPrevented()) {
                    monstersKilled++;
                    mi.remove();
                }
                continue;
            }

            if (m.isReadyToRemove()) {
                monstersKilled++;
                mi.remove();
            }
        }

            scoreLabel.setText("Score: " + monstersKilled);

           if (waveManager.isFinished() && activeMonsters.isEmpty()) {
                waveLabel.setText("You win at wave " + currentWave + "!");
                scoreLabel.setText("Final Score: " + monstersKilled);
                endGame(true);
                return;
            }

            if (activeMonsters.isEmpty() && !waveManager.isFinished()) {
                // Check if we just finished a level boundary (every 3 waves)
                if (currentWave > 0 && currentWave % 3 == 0) {
                    completedLevel = waveManager.getCurrentLevel();
                    saveProgress();
                    state = GameState.LEVEL_COMPLETE;
                    gameStarted = false;
                    soundManager.stopClip("background");
                } else {
                    spawnWave();
                }
            }

        } catch (Exception e) {
            System.out.println("Error during game update: " + e.getMessage());
        }
    }

    // Render

    public void gameRender() {
        Graphics2D g = (Graphics2D) offscreen.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case MENU -> drawMenu(g);
            case PLAYING -> { drawGameScene(g); drawHUD(g); }
            case PAUSED -> { drawGameScene(g); drawHUD(g); drawPauseOverlay(g); }
            case LEVEL_COMPLETE -> { drawGameScene(g); drawLevelCompleteOverlay(g); }
            case GAME_OVER -> drawEndScreen(g, false);
            case WIN -> drawEndScreen(g, true);
        }

        g.dispose();

        Graphics scr = gameArea.getGraphics();
        if (scr != null) {
            scr.drawImage(offscreen, 0, 0, W, H, null);
            scr.dispose();
        }
    }



private void saveProgress() {
    try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
        pw.println(currentWave);
        pw.println(monstersKilled);
        pw.println(completedLevel);
    } catch (IOException ignored) {}
}

private int[] loadProgress() {
    try {
        List<String> lines = Files.readAllLines(Path.of(SAVE_FILE));
        return new int[]{ Integer.parseInt(lines.get(0).trim()),
                          Integer.parseInt(lines.get(1).trim()),
                          Integer.parseInt(lines.get(2).trim()) };
    } catch (Exception e) { return null; }
}






    public boolean isGameStarted() { return gameStarted; }
    public ArrayList<Monster> getMonsters()   { return activeMonsters; }
    public JLabel getScoreLabel() { return scoreLabel; }
    public JLabel getWaveLabel()  { return waveLabel; }
    public JLabel getBulletLabel(){ return bulletLabel; }

    // helpers

    private void endGame(boolean won) {
        state       = won ? GameState.WIN : GameState.GAME_OVER;
        gameStarted = false;
        soundManager.stopClip("background");
    }

    private void spawnWave() {
    if (waveManager.isFinished()) return;

    WaveManager.SpawnData data = waveManager.nextWave();
    currentWave = data.wave;
    waveLabel.setText("Wave: " + currentWave + "  |  Level: " + data.level);

    for (Class<? extends Monster> type : data.monsterTypes) {
        int spawnSide = random.nextInt(2);
        int baseX     = (spawnSide == 0) ? -50 : W + 50;
        int xPos      = (spawnSide == 0)
                ? baseX - random.nextInt(200)
                : baseX + random.nextInt(200);

        Monster m = createMonster(type, xPos, 350);
        m.sharedMonsterList = activeMonsters;

        boolean clash;
        int safety = 0;
        do {
            clash = false;
            for (Monster existing : activeMonsters) {
                if (m.getBoundingRectangle().intersects(existing.getBoundingRectangle())) {
                    m.x += (spawnSide == 0) ? -50 : 50;
                    clash = true;
                }
            }
        } while (clash && ++safety < 10);

        activeMonsters.add(m);
    }
}


private void checkStompCollisions() {
        if (!player.isFalling()) return;

        java.awt.geom.Rectangle2D.Double playerRect = player.getBoundingRectangle();
        int playerFeetY = (int)(playerRect.y + playerRect.height);

        for (Monster m : activeMonsters) {
            if (m.isDead()) continue;

            java.awt.geom.Rectangle2D.Double monsterRect = m.getBoundingRectangle();
            int monsterTopY = (int) monsterRect.y;

            boolean hOverlap = playerRect.x + playerRect.width > monsterRect.x
                             && playerRect.x < monsterRect.x + monsterRect.width;

            boolean inStompZone = playerFeetY >= monsterTopY
                                && playerFeetY <= monsterTopY + 20;

            if (hOverlap && inStompZone) {
                m.takeDamage(STOMP_DAMAGE);
                player.bounce();
                soundManager.playClip("hit", false);
                break;
            }
        }
    }

    // method to create monsters based on class type, used in wave spawning
    private Monster createMonster(Class<? extends Monster> type, int xPos, int yPos) {
        if (type == Snake.class)          return new Snake(gameArea, xPos, yPos, player, treasure);
        if (type == Ghost.class)          return new Ghost(gameArea, xPos, yPos, player, treasure);
        if (type == ShadowWalker.class)   return new ShadowWalker(gameArea, xPos, yPos, player, treasure);
        if (type == ArmoredTurtle.class)  return new ArmoredTurtle(gameArea, xPos, yPos, player, treasure);
        if (type == FireImp.class)        return new FireImp(gameArea, xPos, yPos, player, treasure);
        if (type == SplitSlime.class)     return new SplitSlime(gameArea, xPos, yPos-20, player, treasure);
        if (type == Healer.class)         return new Healer(gameArea, xPos, yPos, player, treasure);
        if (type == ShieldGuardian.class) return new ShieldGuardian(gameArea, xPos, yPos, player, treasure);
        if (type == BerserkerOrc.class)   return new BerserkerOrc(gameArea, xPos, yPos, player, treasure);
        return new Snake(gameArea, xPos, yPos, player, treasure);
    }

    // Scene Drawing

private void drawGameScene(Graphics2D g) {
    g.setColor(new Color(135, 206, 235));
    g.fillRect(0, 0, W, H);
    g.setColor(new Color(155, 118, 83));
    g.fillRect(0, 390, W, H - 390);

    if (treasure != null) treasure.draw(g);
    if (player   != null) player.draw(g);

    for (Monster m : new ArrayList<>(activeMonsters)) m.draw(g);
    for (Bullet  b : new ArrayList<>(bullets))        b.draw(g);
}

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, W, 26);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.drawString(waveLabel.getText(),   10,  18);
        g.drawString(scoreLabel.getText(),  200, 18);
        g.drawString(bulletLabel.getText(), 360, 18);
        g.drawString("ESC = Pause",         670, 18);
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(15, 15, 45));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(25, 55, 110));
        g.fillRect(0, H - 140, W, 140);
        g.setColor(new Color(100, 70, 40));
        g.fillRect(0, H - 85, W, 85);

        g.setFont(new Font("Arial", Font.BOLD, 54));
        g.setColor(new Color(255, 220, 0));
        drawCentred(g, "Treasure Defender", 100);

        g.setFont(new Font("Arial", Font.ITALIC, 18));
        g.setColor(new Color(200, 200, 200));
        drawCentred(g, "Defeat the monsters! and Protect the treasure", 136);

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(new Color(150, 150, 150));
        drawCentred(g, "← →  Move     Mouse  Aim & Shoot     1-9  Switch bullet     ESC  Pause", 415);


        int[] save = loadProgress();
        if (save != null) {
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(new Color(255, 120, 180));
            drawCentred(g, "Saved: Wave: " + save[0] + "  Score: " + save[1] + "  Level: " + save[2], 170);
            drawBtn(g, BTN_MENU_CONTINUE, "Continue", hMenuContinue, new Color(50,70,135), new Color(70,105,195));
        }

        drawBtn(g, BTN_MENU_START, "Start Game", hMenuStart, new Color(30,120,30), new Color(50,190,50));
        drawBtn(g, BTN_MENU_EXIT,  "Exit",        hMenuExit,  new Color(120,30,30), new Color(190,50,50));
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, W, H);

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.WHITE);
        drawCentred(g, "PAUSED", 178);

        drawBtn(g, BTN_PAUSE_RESUME, "Resume",    hPauseResume, new Color(30,120,30), new Color(50,190,50));
        drawBtn(g, BTN_PAUSE_MENU,   "Main Menu", hPauseMenu,   new Color(50,70,135), new Color(70,105,195));
        drawBtn(g, BTN_PAUSE_EXIT,   "Exit",      hPauseExit,   new Color(120,30,30), new Color(190,50,50));
    }

    private void drawEndScreen(Graphics2D g, boolean won) {
        g.setColor(won ? new Color(5, 25, 5) : new Color(25, 5, 5));
        g.fillRect(0, 0, W, H);

        g.setFont(new Font("Arial", Font.BOLD, 58));
        g.setColor(won ? new Color(50, 230, 50) : new Color(230, 50, 50));
        drawCentred(g, won ? "YOU WIN!" : "GAME OVER", 168);

        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        drawCentred(g, scoreLabel.getText() + "   |   Wave: " + currentWave, 232);

        drawBtn(g, BTN_END_MENU, "Main Menu", hEndMenu, new Color(50,70,135), new Color(70,105,195));
        drawBtn(g, BTN_END_EXIT, "Exit",      hEndExit, new Color(120,30,30), new Color(190,50,50));
    }


  private void drawLevelCompleteOverlay(Graphics2D g) {
    g.setColor(new Color(0, 0, 0, 200));
    g.fillRect(0, 0, W, H);

    g.setFont(new Font("Arial", Font.BOLD, 48));
    g.setColor(new Color(255, 215, 0));
    drawCentred(g, "LEVEL " + completedLevel + " COMPLETE!", 130);

    g.setFont(new Font("Arial", Font.PLAIN, 20));
    g.setColor(Color.WHITE);
    drawCentred(g, "Waves cleared: " + currentWave + "   |   Score: " + monstersKilled, 185);

    // Unlocked bullets
    g.setFont(new Font("Arial", Font.BOLD, 16));
    g.setColor(new Color(100, 220, 255));
    drawCentred(g, "Bullets unlocked:", 230);
    g.setFont(new Font("Arial", Font.PLAIN, 15));
    StringBuilder bullets = new StringBuilder();
    for (BulletType bt : waveManager.getUnlockedBullets()) {
        if (bullets.length() > 0) bullets.append("  |  ");
        bullets.append(bt.name());
    }
    drawCentred(g, bullets.toString(), 258);

    // Next level preview
    int nextLevel = completedLevel + 1;
    g.setFont(new Font("Arial", Font.ITALIC, 15));
    g.setColor(new Color(180, 180, 180));
    drawCentred(g, "Next: Level " + nextLevel + "  (Waves " + (currentWave + 1) + "–" + (currentWave + 3) + ")", 300);

    drawBtn(g, BTN_LEVEL_CONTINUE, "Continue",  hLevelContinue, new Color(30,120,30),  new Color(50,190,50));
    drawBtn(g, BTN_LEVEL_MENU,     "Main Menu", hLevelMenu,     new Color(50,70,135),   new Color(70,105,195));
}











    private void drawCentred(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (W - fm.stringWidth(text)) / 2, y);
    }

    private void drawBtn(Graphics2D g, Rectangle r, String label,
                         boolean hover, Color normal, Color hot) {
        g.setColor(hover ? hot : normal);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label,
                r.x + (r.width  - fm.stringWidth(label)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    // KeyListener

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {

            case KeyEvent.VK_ESCAPE -> {
                if      (state == GameState.PLAYING) state = GameState.PAUSED;
                else if (state == GameState.PAUSED)  state = GameState.PLAYING;
            }

            case KeyEvent.VK_LEFT  -> { if (player != null) player.setMovingLeft(true);  }
            case KeyEvent.VK_RIGHT -> { if (player != null) player.setMovingRight(true); }
            case KeyEvent.VK_SPACE -> { if (player != null && gameStarted) player.jump(); }

            case KeyEvent.VK_1 -> setBulletType(BulletType.BASIC);
            case KeyEvent.VK_2 -> setBulletType(BulletType.FIRE);
            case KeyEvent.VK_3 -> setBulletType(BulletType.FREEZE);
            case KeyEvent.VK_4 -> setBulletType(BulletType.ELECTRIC);
            case KeyEvent.VK_5 -> setBulletType(BulletType.SPIRIT);
            case KeyEvent.VK_6 -> setBulletType(BulletType.RAPID);
            case KeyEvent.VK_7 -> setBulletType(BulletType.PIERCING);
            case KeyEvent.VK_8 -> setBulletType(BulletType.EXPLOSIVE);
            case KeyEvent.VK_9 -> setBulletType(BulletType.TELEPORT);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (player == null) return;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  player.setMovingLeft(false);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) player.setMovingRight(false);
    }
    @Override public void keyTyped   (KeyEvent e) {}

    // MouseListener

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        requestFocusInWindow();

        switch (state) {
            case MENU     -> {
                if      (BTN_MENU_START.contains(x, y)) startGame();
                else if (BTN_MENU_EXIT.contains(x, y))  System.exit(0);
                else if (BTN_MENU_CONTINUE.contains(x, y)) {
                int[] save = loadProgress();
                if (save != null) {
                    createGameEntities();
                    // fast-forward WaveManager to the saved wave
                    for (int i = 0; i < save[0]; i++) waveManager.nextWave();
                    currentWave    = save[0];
                    monstersKilled = save[1];
                    completedLevel = save[2];
                    scoreLabel.setText("Score: " + monstersKilled);
                    state       = GameState.PLAYING;
                    gameStarted = true;
                    soundManager.playClip("background", true);
                    spawnWave();
                }
            }
            }
            case PLAYING  -> shootBullet(x, y);
            case PAUSED   -> {
                if      (BTN_PAUSE_RESUME.contains(x, y)) state = GameState.PLAYING;
                else if (BTN_PAUSE_MENU.contains(x, y))   stopGame();
                else if (BTN_PAUSE_EXIT.contains(x, y))   System.exit(0);
            }

            case LEVEL_COMPLETE -> {
                    if (BTN_LEVEL_CONTINUE.contains(x, y)) {
                        gameStarted = true;
                        state = GameState.PLAYING;
                    //    soundManager.playClip("background", true);
                        spawnWave();
                    } else if (BTN_LEVEL_MENU.contains(x, y)) {
                        stopGame();
                    }
                }



            case GAME_OVER, WIN -> {
                if      (BTN_END_MENU.contains(x, y)) state = GameState.MENU;
                else if (BTN_END_EXIT.contains(x, y)) System.exit(0);
            }

        }
    }

    @Override public void mouseClicked (MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered (MouseEvent e) {}
    @Override public void mouseExited  (MouseEvent e) {}

    
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        updateHover();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        shootBullet(mouseX, mouseY);
        updateHover();
    }

    private void updateHover() {
        int x = mouseX, y = mouseY;
        hMenuStart = BTN_MENU_START.contains(x, y);
        hMenuExit = BTN_MENU_EXIT.contains(x, y);
        hPauseResume = BTN_PAUSE_RESUME.contains(x, y);
        hPauseMenu = BTN_PAUSE_MENU.contains(x, y);
        hPauseExit = BTN_PAUSE_EXIT.contains(x, y);
        hEndMenu = BTN_END_MENU.contains(x, y);
        hEndExit = BTN_END_EXIT.contains(x, y);
        hLevelContinue = BTN_LEVEL_CONTINUE.contains(x, y);
        hLevelMenu     = BTN_LEVEL_MENU.contains(x, y);
        hMenuContinue = BTN_MENU_CONTINUE.contains(x, y);
    }
}
