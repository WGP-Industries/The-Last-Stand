import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DamagePack {

    public enum Tier { BRONZE, SILVER, GOLD }

    private final int x, y;
    public static final int SIZE = 44;
    private boolean purchased = false;

    public final Tier  tier;
    public final int   cost;
    public final float buffMultiplier;

    private final long createdTime = System.currentTimeMillis();
    private long flashExpiry = 0;

    private static final Color C_BRONZE_BG  = new Color(140,  80,  28);
    private static final Color C_BRONZE_FG  = new Color(205, 127,  50);
    private static final Color C_BRONZE_RIM = new Color(255, 180,  80);

    private static final Color C_SILVER_BG  = new Color( 90,  90,  95);
    private static final Color C_SILVER_FG  = new Color(180, 185, 195);
    private static final Color C_SILVER_RIM = new Color(230, 235, 245);

    private static final Color C_GOLD_BG    = new Color(160, 120,   0);
    private static final Color C_GOLD_FG    = new Color(255, 215,   0);
    private static final Color C_GOLD_RIM   = new Color(255, 240, 120);

    public DamagePack(int worldX, int worldY, Tier tier) {
        this.x    = worldX;
        this.y    = worldY;
        this.tier = tier;

        switch (tier) {
            case BRONZE -> { cost = 3; buffMultiplier = 1.25f; }
            case SILVER -> { cost = 5; buffMultiplier = 1.50f; }
            default     -> { cost = 8; buffMultiplier = 2.00f; }
        }
    }

    public void draw(Graphics2D g2) {
        if (purchased) return;

        long  now   = System.currentTimeMillis();
        long  age   = now - createdTime;

        double bob   = Math.sin(age * 0.005) * 5.0;
        int    drawY = (int)(y + bob);

        double pulse    = Math.sin(age * 0.007) * 0.06 + 0.94;
        int    drawSize = (int)(SIZE * pulse);
        int    offX     = (SIZE - drawSize) / 2;
        int    cx       = x + SIZE / 2;
        int    cy       = drawY + SIZE / 2;

        Color bgCol  = tierBg();
        Color fgCol  = tierFg();
        Color rimCol = tierRim();

        for (int r = 30; r >= 6; r -= 5) {
            float a = 0.035f * (30 - r + 6);
            g2.setColor(new Color(fgCol.getRed()/255f,
                                  fgCol.getGreen()/255f,
                                  fgCol.getBlue()/255f,
                                  Math.min(1f, a)));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }

        int hs = drawSize / 2;
        int[] px = { cx,        cx + hs, cx,        cx - hs };
        int[] py = { drawY,     cy,      drawY + drawSize, cy };
        g2.setColor(bgCol);
        g2.fillPolygon(px, py, 4);
        g2.setColor(rimCol);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawPolygon(px, py, 4);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(fgCol);
        Graphics2D fg2 = (Graphics2D) g2.create();
        fg2.translate(cx, cy);
        int sw = drawSize / 8;
        int sl = drawSize * 2 / 3;
        fg2.rotate(Math.toRadians(45));
        fg2.fillRect(-sl / 2, -sw / 2, sl, sw);
        fg2.rotate(Math.toRadians(90));
        fg2.fillRect(-sl / 2, -sw / 2, sl, sw);
        fg2.dispose();

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(rimCol);
        String multLabel = "×" + (buffMultiplier == (int) buffMultiplier
                ? String.valueOf((int) buffMultiplier)
                : String.valueOf(buffMultiplier));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(multLabel,
                x + (SIZE - fm.stringWidth(multLabel)) / 2,
                drawY + SIZE + 14);

        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(255, 215, 80));
        String costLabel = cost + " coins";
        g2.drawString(costLabel,
                x + (SIZE - g2.getFontMetrics().stringWidth(costLabel)) / 2,
                drawY + SIZE + 26);

        if (now < flashExpiry) {
            float alpha = Math.min(1f, (flashExpiry - now) / 600f);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(1f, 0.3f, 0.3f, alpha));
            String msg = "Need " + cost + " coins!";
            g2.drawString(msg,
                    x + (SIZE - g2.getFontMetrics().stringWidth(msg)) / 2,
                    drawY - 10);
        }
    }

    public boolean tryPurchase(Rectangle2D.Double playerRect, int coins) {
        if (purchased) return false;

        Rectangle2D.Double myRect = new Rectangle2D.Double(x, y, SIZE, SIZE);
        if (!myRect.intersects(playerRect)) return false;

        if (coins < cost) {
            if (System.currentTimeMillis() >= flashExpiry) {
                flashExpiry = System.currentTimeMillis() + 1800;
            }
            return false;
        }

        purchased = true;
        return true;
    }

    public boolean isPurchased()         { return purchased; }
    public int     getCost()             { return cost; }
    public float   getBuffMultiplier()   { return buffMultiplier; }
    public Tier    getTier()             { return tier; }

    private Color tierBg()  { return switch(tier){ case BRONZE->C_BRONZE_BG; case SILVER->C_SILVER_BG; default->C_GOLD_BG; }; }
    private Color tierFg()  { return switch(tier){ case BRONZE->C_BRONZE_FG; case SILVER->C_SILVER_FG; default->C_GOLD_FG; }; }
    private Color tierRim() { return switch(tier){ case BRONZE->C_BRONZE_RIM;case SILVER->C_SILVER_RIM;default->C_GOLD_RIM;}; }
}
