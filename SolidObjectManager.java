import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SolidObjectManager {

    private List<SolidObject> solidObjects = new ArrayList<>();

    private static final Color WOOD  = new Color(101,  67,  33);
    private static final Color STONE = new Color(115, 100,  82);
    private static final Color DARK  = new Color( 70,  55,  40);

    public SolidObjectManager() {
        loadLevel(1);
    }

    /**
     * Replaces the current platform set with the layout for the given level.
     * Level 1  – no platforms (open flat map).
     * Level 2  – two wide wooden platforms, monsters drop from them.
     * Level 3  – three stone platforms at varying heights.
     * Level 4  – four narrower platforms, more obstacles.
     * Level 5  – six platforms creating a gauntlet.
     */
    public void loadLevel(int level) {
        solidObjects.clear();
        int W = WorldConfig.WORLD_W;

        switch (level) {

            case 1 -> { /* no platforms */ }

            case 2 -> {
                // Two wide platforms, left and right of the treasure
                addPlatform(  380, 200, 320, 18, WOOD);
                addPlatform(W-700, 200, 320, 18, WOOD);
            }

            case 3 -> {
                // Three stone platforms; the centre one is higher
                addPlatform(  300, 215, 270, 20, STONE);
                addPlatform(W/2-135, 170, 270, 20, STONE);
                addPlatform(W-570, 215, 270, 20, STONE);
            }

            case 4 -> {
                // Four narrower platforms – player must navigate around them
                addPlatform(  250, 220, 210, 20, WOOD);
                addPlatform(  730, 175, 210, 20, DARK);
                addPlatform(W-940, 190, 210, 20, DARK);
                addPlatform(W-460, 175, 210, 20, WOOD);
            }

            case 5 -> {
                // Six-platform gauntlet across the full world width
                addPlatform(  220, 220, 185, 20, STONE);
                addPlatform(  620, 180, 185, 20, STONE);
                addPlatform(  980, 155, 185, 20, DARK);
                addPlatform( 1390, 180, 185, 20, DARK);
                addPlatform( 1740, 160, 185, 20, STONE);
                addPlatform( 2090, 220, 185, 20, STONE);
            }

            default -> loadLevel(Math.min(level, 5));   // clamp to last layout
        }
    }

    private void addPlatform(int x, int y, int w, int h, Color c) {
        solidObjects.add(new SolidObject(x, y, w, h, c));
    }

    // physics and collision helpers

    public int getLandingY(int x, int width, int currentY, int height) {
        final int floorSurface = WorldConfig.FLOOR_Y - height;
        int landingY = floorSurface;

        for (SolidObject so : solidObjects) {
            int platTop     = so.getY();
            int platSurface = platTop - height;   // where the object rests on this platform

            boolean hOverlap = x + width > so.getX() && x < so.getX() + so.getWidth();

            boolean approachingFromAbove = (currentY + height) <= platTop + 22;

            if (hOverlap && approachingFromAbove && platSurface < floorSurface) {
                landingY = platSurface;
            }
        }
        return landingY;
    }

    // Spawn points for monsters to drop from when they spawn in the air 
    public List<int[]> getPlatformSpawnPoints() {
        List<int[]> pts = new ArrayList<>();
        for (SolidObject so : solidObjects) {
            pts.add(new int[]{
                so.getX() + so.getWidth() / 2,  
                so.getY(),                        
                so.getWidth()
            });
        }
        return pts;
    }


    public List<SolidObject> getSolidObjects() { return solidObjects; }

    public void draw(Graphics2D g2) {
        for (SolidObject so : solidObjects) {
            so.draw(g2);
            // draw a subtle darker edge
            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawLine(so.getX(), so.getY(), so.getX() + so.getWidth(), so.getY());
        }
    }

    public SolidObject collidesWith(Rectangle2D.Double boundingRectangle) {
        for (SolidObject so : solidObjects) {
            if (so.getBoundingRectangle().intersects(boundingRectangle)) return so;
        }
        return null;
    }

    public boolean onSolidObject(int x, int width) {
        for (SolidObject so : solidObjects) {
            int right = so.getX() + so.getWidth() - 1;
            if (x + width > so.getX() && x <= right) return true;
        }
        return false;
    }
}
