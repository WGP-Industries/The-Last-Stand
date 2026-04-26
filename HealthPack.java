import java.awt.*;
import java.awt.geom.Rectangle2D;

public class HealthPack {

    private int x, y;
    static final int SIZE = 36;
    private boolean collected = false;
    private final int healAmount;
    private final long createdTime;

    public HealthPack(int x, int y, int healAmount) {
        this.x = x;
        this.y = y;
        this.healAmount = healAmount;
        this.createdTime = System.currentTimeMillis();
    }

    public void draw(Graphics2D g2) {
        if (collected) return;

        long now = System.currentTimeMillis();
        double bob    = Math.sin((now - createdTime) * 0.004) * 4.0;
        int drawY  = (int)(y + bob);

        double pulse    = Math.sin((now - createdTime) * 0.006) * 0.08 + 0.92;
        int drawSize = (int)(SIZE * pulse);
        int offX     = (SIZE - drawSize) / 2;

        for (int r = 26; r >= 6; r -= 4) {
            float alpha = 0.04f * (26 - r);
            g2.setColor(new Color(0.15f, 0.9f, 0.25f, alpha));
            g2.fillOval(x + SIZE / 2 - r, drawY + SIZE / 2 - r, r * 2, r * 2);
        }

        g2.setColor(new Color(190, 40, 40));
        g2.fillRoundRect(x + offX, drawY, drawSize, drawSize, 10, 10);

        int cw  = drawSize / 4;
        int ch  = drawSize * 3 / 4;
        int cx  = x + offX + (drawSize - cw) / 2;
        int cy  = drawY + (drawSize - ch) / 2;
        g2.setColor(Color.WHITE);
        g2.fillRect(cx, cy, cw, ch);
        g2.fillRect(x + offX + (drawSize - ch) / 2, drawY + (drawSize - cw) / 2, ch, cw);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(230, 90, 90));
        g2.drawRoundRect(x + offX, drawY, drawSize, drawSize, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 255, 160));
        FontMetrics fm = g2.getFontMetrics();
        String label = "+" + healAmount + " HP";
        g2.drawString(label, x + (SIZE - fm.stringWidth(label)) / 2, drawY + SIZE + 14);
    }

    public boolean checkCollision(Rectangle2D.Double playerRect) {
        if (collected) return false;
        Rectangle2D.Double myRect = new Rectangle2D.Double(x, y, SIZE, SIZE);
        if (myRect.intersects(playerRect)) {
            collected = true;
            return true;
        }
        return false;
    }

    public boolean isCollected()  { return collected; }
    public int getHealAmount() { return healAmount; }
}