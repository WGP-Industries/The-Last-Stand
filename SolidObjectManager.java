import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SolidObjectManager {

    private List<SolidObject> solidObjects = new ArrayList<>();

    private static final Color WOOD = new Color(101, 67, 33);
    private static final Color STONE = new Color(115, 100, 82);
    private static final Color DARK = new Color(70, 55, 40);

    public SolidObjectManager() {
        loadLevel(1);
    }

    public void loadLevel(int level) {
        solidObjects.clear();
        int W = WorldConfig.WORLD_W;

        switch (level) {

            case 1 -> {
            }

            case 2 -> {
                addPlatform(320, 200, 200, 18, WOOD);
                addPlatform(750, 280, 180, 18, STONE);
                addPlatform(W - 950, 280, 180, 18, STONE);
                addPlatform(W - 520, 200, 200, 18, WOOD);
            }

            case 3 -> {
                addPlatform(260, 190, 220, 20, STONE);
                addPlatform(800, 240, 200, 20, STONE);
                addPlatform(W - 580, 215, 220, 20, STONE);
                addPlatform(W - 220, 190, 160, 20, DARK);
            }

            case 4 -> {
                addPlatform(220, 220, 190, 20, WOOD);
                addPlatform(620, 175, 190, 20, DARK);
                addPlatform(920, 240, 170, 20, STONE);
                addPlatform(W - 900, 175, 170, 20, STONE);
                addPlatform(W - 610, 220, 190, 20, DARK);
                addPlatform(W - 220, 175, 190, 20, WOOD);
            }

            case 5 -> {
                addPlatform(180, 220, 170, 20, STONE);
                addPlatform(520, 180, 170, 20, STONE);
                addPlatform(860, 155, 170, 20, DARK);
                addPlatform(W - 860, 180, 170, 20, DARK);
                addPlatform(W - 520, 160, 170, 20, STONE);
                addPlatform(W - 180, 220, 170, 20, STONE);
            }

            // clamp anything above level 5 to level 5
            default -> loadLevel(Math.min(level, 5));
        }
    }

    private void addPlatform(int x, int y, int w, int h, Color c) {
        solidObjects.add(new SolidObject(x, y, w, h, c));
    }

    // returns the Y position an entity should land on given its current position,
    // checking platforms first then falling back to the floor
    public int getLandingY(int x, int width, int currentY, int height) {
        final int floorSurface = WorldConfig.FLOOR_Y - height;
        int landingY = floorSurface;

        for (SolidObject so : solidObjects) {
            int platSurface = so.getY() - height;
            boolean hOverlap = x + width > so.getX() && x < so.getX() + so.getWidth();
            boolean approachingFromAbove = (currentY + height) <= so.getY() + 22;

            if (hOverlap && approachingFromAbove && platSurface < floorSurface) {
                landingY = platSurface;
            }
        }
        return landingY;
    }

    // each platform gets one portal centered on it; if there are no platforms
    public List<int[]> getPortalSpawnData() {
        List<int[]> pts = new ArrayList<>();

        // Always add left and right edge portals (where monsters enter/respawn)
        pts.add(new int[] { -50, WorldConfig.FLOOR_Y - 18, 52, 1 }); // left ground
        pts.add(new int[] { WorldConfig.WORLD_W - 2, WorldConfig.FLOOR_Y - 18, 52, 1 }); // right ground

        // Platform portals (levels 2+)
        for (SolidObject so : solidObjects) {
            pts.add(new int[] { so.getX(), so.getY(), so.getWidth(), 0 });
        }

        return pts;
    }

    public void draw(Graphics2D g2) {
        for (SolidObject so : solidObjects) {
            so.draw(g2);
            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawLine(so.getX(), so.getY(), so.getX() + so.getWidth(), so.getY());
        }
    }

    public List<SolidObject> getSolidObjects() {
        return solidObjects;
    }

    public SolidObject collidesWith(Rectangle2D.Double boundingRectangle) {
        for (SolidObject so : solidObjects) {
            if (so.getBoundingRectangle().intersects(boundingRectangle))
                return so;
        }
        return null;
    }

    public boolean onSolidObject(int x, int width) {
        for (SolidObject so : solidObjects) {
            if (x + width > so.getX() && x <= so.getX() + so.getWidth() - 1)
                return true;
        }
        return false;
    }
}