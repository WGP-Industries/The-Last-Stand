import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
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

    private enum GameState { MENU, PLAYING, PAUSED, GAME_OVER, WIN, LEVEL_COMPLETE }
    private volatile GameState state = GameState.MENU;

    private static final int W = WorldConfig.VIEW_W;
    private static final int H = WorldConfig.VIEW_H;



    private BackgroundManager surfaceBgManager;
    private BackgroundManager undergroundBgManager;
    private int lastCamX = 0;   // tracks previous camX to derive movement delta
    private int lastPlayerY = 0;
    private JPanel gameArea;

    private BufferedImage offscreen;
    private Thread        gameThread;
    private volatile boolean isRunning;

    private final SoundManager soundManager = SoundManager.getInstance();
    private final Random random = new Random();

    private double camX = 0;
    private double camY = 0;

    private static final int STOMP_DAMAGE = 40;

    private Player             player;
    private Treasure           treasure;
    private ArrayList<Bullet>  bullets;
    private ArrayList<Monster> activeMonsters;

    private int  monstersKilled;
    private int  currentWave;
    private long lastShotTime;
    private int  completedLevel = 0;
    private boolean gameStarted;

    private JLabel scoreLabel  = new JLabel("Score: 0");
    private JLabel waveLabel   = new JLabel("Wave: 0");
    private JLabel bulletLabel = new JLabel("Bullet: BASIC  [1-9]");

    private static final String SAVE_FILE = "data/save.dat";

    private final WaveManager waveManager    = new WaveManager();
    private SpawnData         currentSpawnData;
    private int mouseX = W / 2;
    private int mouseY = H / 2;

    private int coins = 0;

    private String shopMessage     = "";
    private long   shopMessageExpiry = 0;

    private boolean underground     = false;
    private int     transitionTicks = 0;
    private static final int TRANSITION_DURATION = 12;

    private ArrayList<HealthPack> healthPacks          = new ArrayList<>();
    private static final int      HEALTH_PACK_HEAL     = 75;
    private static final int      HEALTH_PACK_RESPAWN  = 360;
    private int                   healthPackRespawnTimer = 0;

    private ArrayList<DamagePack> damagePacks = new ArrayList<>();

    private static final Rectangle BTN_MENU_START     = new Rectangle(380, 234, 200, 50);
    private static final Rectangle BTN_MENU_EXIT      = new Rectangle(380, 304, 200, 50);
    private static final Rectangle BTN_PAUSE_RESUME   = new Rectangle(380, 190, 200, 50);
    private static final Rectangle BTN_PAUSE_MENU     = new Rectangle(380, 255, 200, 50);
    private static final Rectangle BTN_PAUSE_EXIT     = new Rectangle(380, 320, 200, 50);
    private static final Rectangle BTN_END_MENU       = new Rectangle(380, 270, 200, 50);
    private static final Rectangle BTN_END_EXIT       = new Rectangle(380, 340, 200, 50);
    private static final Rectangle BTN_LEVEL_CONTINUE = new Rectangle(280, 315, 160, 45);
    private static final Rectangle BTN_LEVEL_MENU     = new Rectangle(510, 315, 160, 45);
    private static final Rectangle BTN_MENU_CONTINUE  = new Rectangle(380, 176, 200, 50);

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
        int worldCx = WorldConfig.WORLD_W / 2;

        player         = new Player(gameArea, worldCx, 0);
        treasure       = new Treasure(worldCx, WorldConfig.FLOOR_Y - 45);
        activeMonsters = new ArrayList<>();
        bullets        = new ArrayList<>();
        waveManager.reset();

        monstersKilled = 0;
        currentWave    = 0;
        lastShotTime   = 0;
        coins          = 0;
        underground    = false;
        transitionTicks = 0;
        healthPackRespawnTimer = 0;
        camX           = worldCx - W / 2;

        surfaceBgManager     = new BackgroundManager(this, 1);
        undergroundBgManager = new BackgroundManager(this, 1);
        lastCamX = (int) camX;

        scoreLabel.setText("Score: 0");
        waveLabel.setText("Wave: 0");

        spawnHealthPacks();
        spawnDamagePacks();
    }

    private void spawnHealthPacks() {
        healthPacks.clear();
        int packY = WorldConfig.FLOOR_Y - HealthPack.SIZE - 8;
        int[] xs = {
            (int)(WorldConfig.WORLD_W * 0.10),
            (int)(WorldConfig.WORLD_W * 0.33),
            (int)(WorldConfig.WORLD_W * 0.60),
            (int)(WorldConfig.WORLD_W * 0.87)
        };
        for (int x : xs) healthPacks.add(new HealthPack(x, packY, HEALTH_PACK_HEAL));
    }

    private void spawnDamagePacks() {
        damagePacks.clear();
        int packY = WorldConfig.FLOOR_Y - DamagePack.SIZE - 8;
        damagePacks.add(new DamagePack(
                (int)(WorldConfig.WORLD_W * 0.22), packY, DamagePack.Tier.BRONZE));
        damagePacks.add(new DamagePack(
                (int)(WorldConfig.WORLD_W * 0.50), packY, DamagePack.Tier.SILVER));
        damagePacks.add(new DamagePack(
                (int)(WorldConfig.WORLD_W * 0.78), packY, DamagePack.Tier.GOLD));
    }

    public void startGame() {
        if (!gameStarted) {
            createGameEntities();
            spawnWave();
            state       = GameState.PLAYING;
            gameStarted = true;
            soundManager.playClip("background", true);
        }
    }

    public void stopGame() {
        state           = GameState.MENU;
        gameStarted     = false;
        underground     = false;
        transitionTicks = 0;
        soundManager.stopClip("background");
    }

    public void shootBullet(int screenMouseX, int screenMouseY) {
        if (player == null || !gameStarted || underground) return;
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= player.getCurrentCooldown()) {
            int worldMouseX = (int)(screenMouseX + camX);
            bullets.add(player.shoot(worldMouseX, screenMouseY));
            soundManager.playClip("shoot", false);
            lastShotTime = now;
        }
    }

    public void setBulletType(BulletType type) {
        if (player == null) return;
        if (!waveManager.getUnlockedBullets().contains(type)) {
            bulletLabel.setText("Bullet: " + type.name() + " (locked!)  [1-9]");
            return;
        }
        player.setBulletType(type);
        bulletLabel.setText("Bullet: " + type.name() + "  [1-9]");
    }

private void updateCamera() {
    if (player == null) return;
    double targetX = player.getX() + player.getWidth() / 2.0 - W / 2.0;
    camX = Math.max(0, Math.min(targetX, WorldConfig.WORLD_W - W));

    int newCamX = (int) camX;
    int delta = newCamX - lastCamX;
    if (delta > 0) {
        for (int i = 0; i < delta; i++) surfaceBgManager.moveRight();
    } else if (delta < 0) {
        for (int i = 0; i < -delta; i++) surfaceBgManager.moveLeft();
    }
    lastCamX = newCamX;

    float[] yAmounts = {0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f};
    if (player.getY() < lastPlayerY) surfaceBgManager.moveUp(yAmounts);
    else if (player.getY() > lastPlayerY) surfaceBgManager.moveDown(yAmounts);
    lastPlayerY = player.getY();
}

    public void gameUpdate() {
        if (treasure != null && treasure.isDestroyed()) {
            waveLabel.setText("Game over at wave: " + currentWave + "!");
            scoreLabel.setText("Final Score: " + monstersKilled);
            endGame(false);
            return;
        }

        if (transitionTicks > 0) {
            transitionTicks--;
            if (transitionTicks == TRANSITION_DURATION / 2) {
                underground = !underground;
                if (underground) bullets.clear();
            }
        }

        if (player != null) {
            player.updatePhysics();
            updateCamera();
            if (!underground) checkStompCollisions();
        }

        if (underground && transitionTicks == 0 && player != null) {
            Rectangle2D.Double pRect = player.getBoundingRectangle();

            for (HealthPack hp : healthPacks) {
                if (hp.checkCollision(pRect)) {
                    treasure.heal(HEALTH_PACK_HEAL);
                    showShopMessage("+" + HEALTH_PACK_HEAL + " Treasure HP!", new Color(80, 255, 120));
                    soundManager.playClip("heal", false);
                }
            }
            healthPacks.removeIf(HealthPack::isCollected);
            if (healthPacks.isEmpty()) {
                healthPackRespawnTimer++;
                if (healthPackRespawnTimer >= HEALTH_PACK_RESPAWN) {
                    spawnHealthPacks();
                    healthPackRespawnTimer = 0;
                }
            } else {
                healthPackRespawnTimer = 0;
            }

            for (DamagePack dp : damagePacks) {
                if (dp.isPurchased()) continue;
                if (dp.tryPurchase(pRect, coins)) {
                    coins -= dp.getCost();
                    player.applyDamageBuff(dp.getBuffMultiplier());
                    int pct = Math.round((dp.getBuffMultiplier() - 1f) * 100);
                    showShopMessage("Damage +" + pct + "%!  (" + dp.getCost() + " coins spent)",
                                    new Color(255, 215, 0));
                    soundManager.playClip("heal", false);
                }
            }
        }

        try {
            for (Monster m : activeMonsters) {
                if (m instanceof Healer h) h.move(activeMonsters);
                else                       m.move();
            }

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

            for (Bullet b : new ArrayList<>(bullets)) {
                if (b instanceof ElectricBullet eb) {
                    Iterator<ChainFX> it = eb.getChainFXList().iterator();
                    while (it.hasNext()) {
                        ChainFX fx = it.next();
                        fx.tick();
                        if (!fx.isActive()) it.remove();
                    }
                }
            }

            ArrayList<Monster> toAdd = new ArrayList<>();
            for (Monster m : activeMonsters) {
                if (m instanceof SplitSlime ss && !(m instanceof MiniSlime)) {
                    ArrayList<MiniSlime> minis = ss.drainPendingMinis();
                    for (MiniSlime mini : minis) mini.sharedMonsterList = activeMonsters;
                    toAdd.addAll(minis);
                }
            }
            activeMonsters.addAll(toAdd);

            Iterator<Monster> mi = activeMonsters.iterator();
            while (mi.hasNext()) {
                Monster m = mi.next();
                if (m instanceof MiniSlime mini) {
                    if (mini.isReadyToRemove()) mi.remove();
                    continue;
                }
                if (m instanceof SplitSlime ss) {
                    if ((ss.isReadyToRemove() && ss.isFullyDead())
                     || (ss.isReadyToRemove() && ss.isSplitPrevented())) {
                        monstersKilled++;
                        coins++;
                        mi.remove();
                    }
                    continue;
                }
                if (m.isReadyToRemove()) {
                    monstersKilled++;
                    coins++;
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
                if (currentWave > 0 && currentWave % 3 == 0) {
                    completedLevel = waveManager.getCurrentLevel();
                    saveProgress();
                    state       = GameState.LEVEL_COMPLETE;
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

    private Color shopMessageColor = Color.WHITE;
    private void showShopMessage(String msg, Color col) {
        shopMessage      = msg;
        shopMessageExpiry = System.currentTimeMillis() + 2400;
        shopMessageColor  = col;
    }

    public void gameRender() {
         Graphics2D g = (Graphics2D) offscreen.getGraphics();
    g.setTransform(new java.awt.geom.AffineTransform()); // reset every frame
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case MENU     -> drawMenu(g);

            case PLAYING  -> {
                drawWorldScene(g);
                drawHUD(g);
                drawTransitionOverlay(g);
            }
            case PAUSED   -> {
                drawWorldScene(g);
                drawHUD(g);
                drawTransitionOverlay(g);
                drawPauseOverlay(g);
            }
            case LEVEL_COMPLETE -> {
                applyCamera(g);
                drawSurfaceContent(g);
                restoreCamera(g);
                drawLevelCompleteOverlay(g);
            }
            case GAME_OVER -> drawEndScreen(g, false);
            case WIN       -> drawEndScreen(g, true);
        }

        g.dispose();

        Graphics scr = gameArea.getGraphics();
        if (scr != null) { scr.drawImage(offscreen, 0, 0, W, H, null); scr.dispose(); }
    }

   private void drawWorldScene(Graphics2D g) {
    if (underground) {
        // Underground keeps procedural drawing, no parallax yet
        applyCamera(g);
        drawUndergroundContent(g);
        restoreCamera(g);
    } else {
        // Draw parallax in screen space BEFORE camera offset
        surfaceBgManager.draw(g);
        applyCamera(g);
        drawSurfaceContent(g);
        restoreCamera(g);
    }
}

    private void applyCamera(Graphics2D g) {
        g.translate(-(int) camX, 0);
    }

    private void restoreCamera(Graphics2D g) {
        g.translate((int) camX, 0);
    }

    private void drawSurfaceContent(Graphics2D g) {
        // tile grass&road and tree_face across world in world space
        Image grassRoad = ImageManager.loadImage("images/surface/grass&road.png");
        Image treeFace  = ImageManager.loadImage("images/surface/tree_face.png");

        int imgW = WorldConfig.VIEW_H * 1920 / 1080; // same width Background uses
        int imgH = WorldConfig.VIEW_H;

        for (int tx = 0; tx < WorldConfig.WORLD_W; tx += imgW) {
            g.drawImage(grassRoad, tx, 0, imgW, imgH, null);
            g.drawImage(treeFace,  tx, 0, imgW, imgH, null);
        }

        if (treasure != null)  treasure.draw(g);
        if (player != null)    player.draw(g);
        for (Monster m : new ArrayList<>(activeMonsters)) m.draw(g);
        for (Bullet  b : new ArrayList<>(bullets))        b.draw(g);
        for (Bullet b : new ArrayList<>(bullets)) {
            if (b instanceof ElectricBullet eb)
                for (ChainFX fx : eb.getChainFXList()) fx.draw(g);
        }
    }

    private void drawUndergroundContent(Graphics2D g) {
        int WW = WorldConfig.WORLD_W;
        int FY = WorldConfig.FLOOR_Y;

        g.setColor(new Color(18, 12, 8));
        g.fillRect(0, 0, WW, H);

        g.setColor(new Color(48, 34, 18));
        g.fillRect(0, 0, WW, 58);

        g.setColor(new Color(38, 26, 12));
        for (int bx = 30; bx < WW; bx += 80) {
            int bh = 22 + (bx * 37 % 36);
            int[] px = { bx - 13, bx, bx + 13 };
            int[] py = { 0,        bh, 0 };
            g.fillPolygon(px, py, 3);
        }

        g.setColor(new Color(35, 24, 12));
        g.fillRect(0, 58, WW, FY - 58);

        g.setColor(new Color(55, 38, 18));
        for (int bx = 150; bx < WW; bx += 240) {
            g.fillRect(bx, 60, 3, FY - 60);
        }

        long now = System.currentTimeMillis();
        for (int bx = 100; bx < WW; bx += 180) {
            int gy = 80 + (bx * 53 % 200);
            float gAlpha = (float)(0.5 + 0.5 * Math.sin(now * 0.003 + bx * 0.05));
            g.setColor(new Color(80, 160, 255, (int)(gAlpha * 180)));
            g.fillOval(bx - 3, gy - 3, 6, 6);
            g.setColor(new Color(180, 220, 255, (int)(gAlpha * 120)));
            g.fillOval(bx - 1, gy - 1, 2, 2);
        }

        g.setColor(new Color(68, 48, 24));
        g.fillRect(0, FY, WW, H - FY);
        g.setColor(new Color(88, 64, 30));
        g.fillRect(0, FY, WW, 7);
        g.setColor(new Color(52, 36, 16));
        for (int bx = 60; bx < WW; bx += 190) {
            g.fillOval(bx, FY + 8, 32 + (bx * 7 % 20), 10 + (bx * 3 % 6));
        }

        for (int tx = 140; tx < WW; tx += 280) {
            drawTorch(g, tx, FY - 60);
        }

        for (HealthPack hp : new ArrayList<>(healthPacks)) hp.draw(g);
        for (DamagePack dp : new ArrayList<>(damagePacks)) dp.draw(g);

        if (player != null) player.draw(g);

        if (healthPacks.isEmpty() && healthPackRespawnTimer > 0) {
            int secsLeft = (HEALTH_PACK_RESPAWN - healthPackRespawnTimer) / 20 + 1;
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(new Color(255, 160, 80, 200));
            String msg = "Health packs respawning in " + secsLeft + "s";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, (int)camX + (W - fm.stringWidth(msg)) / 2, H - 40);
        }
    }

    private void drawTorch(Graphics2D g, int cx, int baseY) {
        long   now     = System.currentTimeMillis();
        double flicker = Math.sin(now * 0.013 + cx * 0.009) * 0.28 + 0.72;

        g.setColor(new Color(110, 72, 30));
        g.fillRect(cx - 4, baseY, 8, 24);

        int glowR = (int)(50 * flicker);
        for (int r = glowR; r >= 6; r -= 6) {
            float a = 0.04f * (glowR - r + 6);
            g.setColor(new Color(1f, 0.55f, 0f, Math.min(1f, a)));
            g.fillOval(cx - r, baseY - r, r * 2, r * 2);
        }

        int fw = (int)(14 * flicker);
        int fh = (int)(22 * flicker);
        g.setColor(new Color(255, 110, 0, 220));
        g.fillOval(cx - fw / 2, baseY - fh, fw, fh);

        g.setColor(new Color(255, 230, 30, 230));
        g.fillOval(cx - fw / 4, baseY - fh + 4, fw / 2, fh * 3 / 4);

        g.setColor(new Color(255, 255, 190, 200));
        g.fillOval(cx - 2, baseY - fh - 2, 4, 6);
    }

    private void drawTransitionOverlay(Graphics2D g) {
        int alpha = getTransitionAlpha();
        if (alpha <= 0) return;
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, W, H);
    }

    private int getTransitionAlpha() {
        if (transitionTicks == 0) return 0;
        int halfT = TRANSITION_DURATION / 2;
        if (transitionTicks > halfT) {
            int elapsed = TRANSITION_DURATION - transitionTicks;
            return Math.min(255, (int)(255f * elapsed / halfT));
        } else {
            return Math.min(255, (int)(255f * transitionTicks / halfT));
        }
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, W, 28);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString(waveLabel.getText(),   8,  19);
        g.drawString(scoreLabel.getText(), 200, 19);

        g.setColor(new Color(255, 215, 0));
        g.drawString("⬤ " + coins + " coins", 340, 19);

        if (player != null && player.getDamageMultiplier() > 1.01f) {
            g.setColor(new Color(255, 100, 100));
            g.drawString("DMG x" + String.format("%.2f", player.getDamageMultiplier()), 470, 19);
        }

        g.setColor(Color.WHITE);
        g.drawString(bulletLabel.getText(), 590, 19);

        if (underground) {
            g.setColor(new Color(140, 210, 255));
            g.drawString("▼ UNDERGROUND  [SHIFT: Surface]", 700, 19);
        } else {
            g.setColor(new Color(180, 180, 180));
            g.drawString("SHIFT: Underground", 720, 19);
        }

        g.setColor(Color.WHITE);
        g.drawString("ESC", W - 40, 19);

        if (System.currentTimeMillis() < shopMessageExpiry && !shopMessage.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.setColor(shopMessageColor);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(shopMessage, (W - fm.stringWidth(shopMessage)) / 2, H - 18);
        }

        if (underground && transitionTicks == 0) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(new Color(190, 160, 90, 200));
            String hint = "Walk over Health Packs to heal Treasure  |  Walk over Damage Packs to buy buffs";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hint, (W - fm.stringWidth(hint)) / 2, H - 34);
        }
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(15, 15, 45));   g.fillRect(0, 0, W, H);
        g.setColor(new Color(25, 55, 110));  g.fillRect(0, H - 140, W, 140);
        g.setColor(new Color(100, 70, 40));  g.fillRect(0, H - 85, W, 85);

        g.setFont(new Font("Arial", Font.BOLD, 54));
        g.setColor(new Color(255, 220, 0));
        drawCentred(g, "Treasure Defender", 90);

        g.setFont(new Font("Arial", Font.ITALIC, 17));
        g.setColor(new Color(200, 200, 200));
        drawCentred(g, "Defeat the monsters! Protect the treasure. Explore the underground!", 122);

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(140, 140, 140));
        drawCentred(g, "← →  Move    SPACE  Jump    Mouse  Aim+Shoot    1-9  Bullets"
                     + "    SHIFT  Underground    ESC  Pause", 380);

        int[] save = loadProgress();
        if (save != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(new Color(255, 120, 180));
            drawCentred(g, "Saved: Wave " + save[0] + "  |  Score " + save[1] + "  |  Level " + save[2], 153);
            drawBtn(g, BTN_MENU_CONTINUE, "Continue", hMenuContinue, new Color(50,70,135), new Color(70,105,195));
        }

        drawBtn(g, BTN_MENU_START, "Start Game", hMenuStart, new Color(30,120,30),  new Color(50,190,50));
        drawBtn(g, BTN_MENU_EXIT,  "Exit",        hMenuExit,  new Color(120,30,30), new Color(190,50,50));
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, W, H);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.WHITE);
        drawCentred(g, "PAUSED", 160);
        drawBtn(g, BTN_PAUSE_RESUME, "Resume",    hPauseResume, new Color(30,120,30),  new Color(50,190,50));
        drawBtn(g, BTN_PAUSE_MENU,   "Main Menu", hPauseMenu,   new Color(50,70,135),  new Color(70,105,195));
        drawBtn(g, BTN_PAUSE_EXIT,   "Exit",      hPauseExit,   new Color(120,30,30),  new Color(190,50,50));
    }

    private void drawEndScreen(Graphics2D g, boolean won) {
        g.setColor(won ? new Color(5,25,5) : new Color(25,5,5));
        g.fillRect(0, 0, W, H);
        g.setFont(new Font("Arial", Font.BOLD, 58));
        g.setColor(won ? new Color(50,230,50) : new Color(230,50,50));
        drawCentred(g, won ? "YOU WIN!" : "GAME OVER", 151);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        drawCentred(g, scoreLabel.getText() + "   |   Wave: " + currentWave, 209);
        drawBtn(g, BTN_END_MENU, "Main Menu", hEndMenu, new Color(50,70,135),  new Color(70,105,195));
        drawBtn(g, BTN_END_EXIT, "Exit",      hEndExit, new Color(120,30,30),  new Color(190,50,50));
    }

    private void drawLevelCompleteOverlay(Graphics2D g) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0, 0, W, H);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.setColor(new Color(255, 215, 0));
        drawCentred(g, "LEVEL " + completedLevel + " COMPLETE!", 117);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        drawCentred(g, "Waves: " + currentWave + "   |   Score: " + monstersKilled + "   |   Coins: " + coins, 167);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(100, 220, 255));
        drawCentred(g, "Bullets unlocked:", 207);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        StringBuilder sb = new StringBuilder();
        for (BulletType bt : waveManager.getUnlockedBullets()) {
            if (sb.length() > 0) sb.append("  |  ");
            sb.append(bt.name());
        }
        drawCentred(g, sb.toString(), 232);

        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.setColor(new Color(180,180,180));
        int nextLevel = completedLevel + 1;
        drawCentred(g, "Next: Level " + nextLevel
                + "  (Waves " + (currentWave+1) + "–" + (currentWave+3) + ")", 270);

        drawBtn(g, BTN_LEVEL_CONTINUE, "Continue",  hLevelContinue, new Color(30,120,30), new Color(50,190,50));
        drawBtn(g, BTN_LEVEL_MENU,     "Main Menu", hLevelMenu,     new Color(50,70,135),  new Color(70,105,195));
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

    private void saveProgress() {
        try {
            File file = new File(SAVE_FILE);
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println(currentWave);
                pw.println(monstersKilled);
                pw.println(completedLevel);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private int[] loadProgress() {
        try {
            List<String> lines = Files.readAllLines(Path.of(SAVE_FILE));
            return new int[]{ Integer.parseInt(lines.get(0).trim()),
                              Integer.parseInt(lines.get(1).trim()),
                              Integer.parseInt(lines.get(2).trim()) };
        } catch (Exception e) { return null; }
    }

    private void endGame(boolean won) {
        state       = won ? GameState.WIN : GameState.GAME_OVER;
        gameStarted = false;
        soundManager.stopClip("background");
    }

    private void spawnWave() {
        if (waveManager.isFinished()) return;

        currentSpawnData = waveManager.nextWave();
        currentWave      = currentSpawnData.wave;
        waveLabel.setText("Wave: " + currentWave + "  |  Level: " + currentSpawnData.level);

        int leftCount = 0, rightCount = 0;
        for (Class<? extends Monster> type : currentSpawnData.monsterTypes) {
            int side = random.nextInt(2);
            int xPos;
            if (side == 0) {
                xPos = -200 - (leftCount * 150) - random.nextInt(50);
                leftCount++;
            } else {
                xPos = WorldConfig.WORLD_W + 200 + (rightCount * 150) + random.nextInt(50);
                rightCount++;
            }
            Monster m = createMonster(type, xPos, WorldConfig.FLOOR_Y - 40);
            m.sharedMonsterList = activeMonsters;
            m.collideWithMonster(activeMonsters);
            activeMonsters.add(m);
        }
    }

    private void checkStompCollisions() {
        if (!player.isFalling()) return;
        Rectangle2D.Double pr = player.getBoundingRectangle();
        int feetY = (int)(pr.y + pr.height);
        for (Monster m : activeMonsters) {
            if (m.isDead()) continue;
            Rectangle2D.Double mr = m.getBoundingRectangle();
            int topY = (int) mr.y;
            boolean hOverlap  = pr.x + pr.width > mr.x && pr.x < mr.x + mr.width;
            boolean stompZone = feetY >= topY && feetY <= topY + 40;
            if (hOverlap && stompZone) {
                m.takeDamage(STOMP_DAMAGE);
                player.bounce();
                soundManager.playClip("hit", false);
                break;
            }
        }
    }

    private Monster createMonster(Class<? extends Monster> type, int xPos, int yPos) {
        if (type == Snake.class)          return new Snake         (gameArea, xPos, yPos,      player, treasure);
        if (type == Ghost.class)          return new Ghost         (gameArea, xPos, yPos,      player, treasure);
        if (type == ShadowWalker.class)   return new ShadowWalker  (gameArea, xPos, yPos,      player, treasure);
        if (type == ArmoredTurtle.class)  return new ArmoredTurtle (gameArea, xPos, yPos,      player, treasure);
        if (type == FireImp.class)        return new FireImp       (gameArea, xPos, yPos,      player, treasure);
        if (type == SplitSlime.class)     return new SplitSlime    (gameArea, xPos, yPos - 20, player, treasure);
        if (type == Healer.class)         return new Healer        (gameArea, xPos, yPos,      player, treasure);
        if (type == ShieldGuardian.class) return new ShieldGuardian(gameArea, xPos, yPos,      player, treasure);
        if (type == BerserkerOrc.class)   return new BerserkerOrc  (gameArea, xPos, yPos,      player, treasure);
        return new Snake(gameArea, xPos, yPos, player, treasure);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> {
                if      (state == GameState.PLAYING) state = GameState.PAUSED;
                else if (state == GameState.PAUSED)  state = GameState.PLAYING;
            }
            case KeyEvent.VK_A -> { if (player != null) player.setMovingLeft(true);  }
            case KeyEvent.VK_D -> { if (player != null) player.setMovingRight(true); }
            case KeyEvent.VK_SPACE -> { if (player != null && gameStarted) player.jump(); }
            case KeyEvent.VK_SHIFT -> {
                if (state == GameState.PLAYING && gameStarted && transitionTicks == 0) {
                    transitionTicks = TRANSITION_DURATION;
                }
            }
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
        if (e.getKeyCode() == KeyEvent.VK_A) player.setMovingLeft(false);
        if (e.getKeyCode() == KeyEvent.VK_D) player.setMovingRight(false);
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        requestFocusInWindow();

        switch (state) {
            case MENU -> {
                if      (BTN_MENU_START.contains(x, y))    startGame();
                else if (BTN_MENU_EXIT.contains(x, y))     System.exit(0);
                else if (BTN_MENU_CONTINUE.contains(x, y)) {
                    int[] save = loadProgress();
                    if (save != null) {
                        createGameEntities();
                        for (int i = 0; i < save[0]; i++) waveManager.nextWave();
                        currentWave    = save[0];
                        monstersKilled = save[1];
                        completedLevel = save[2];
                        scoreLabel.setText("Score: " + monstersKilled);
                        spawnWave();
                        gameStarted = true;
                        state       = GameState.PLAYING;
                        soundManager.playClip("background", true);
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
                if      (BTN_LEVEL_CONTINUE.contains(x, y)) { spawnWave(); gameStarted = true; state = GameState.PLAYING; }
                else if (BTN_LEVEL_MENU.contains(x, y))     stopGame();
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
        mouseX = e.getX(); mouseY = e.getY();
        updateHover();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX(); mouseY = e.getY();
        shootBullet(mouseX, mouseY);
        updateHover();
    }

    private void updateHover() {
        int x = mouseX, y = mouseY;
        hMenuStart    = BTN_MENU_START.contains(x, y);
        hMenuExit     = BTN_MENU_EXIT.contains(x, y);
        hPauseResume  = BTN_PAUSE_RESUME.contains(x, y);
        hPauseMenu    = BTN_PAUSE_MENU.contains(x, y);
        hPauseExit    = BTN_PAUSE_EXIT.contains(x, y);
        hEndMenu      = BTN_END_MENU.contains(x, y);
        hEndExit      = BTN_END_EXIT.contains(x, y);
        hLevelContinue = BTN_LEVEL_CONTINUE.contains(x, y);
        hLevelMenu    = BTN_LEVEL_MENU.contains(x, y);
        hMenuContinue = BTN_MENU_CONTINUE.contains(x, y);
    }

    public boolean isGameStarted()          { return gameStarted; }
    public ArrayList<Monster> getMonsters() { return activeMonsters; }
    public JLabel getScoreLabel()           { return scoreLabel; }
    public JLabel getWaveLabel()            { return waveLabel; }
    public JLabel getBulletLabel()          { return bulletLabel; }
}