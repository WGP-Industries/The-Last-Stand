import javax.swing.JPanel;

public class Ghost extends Monster {

    public Ghost(JPanel p, int xPos, int yPos, Player player, Treasure treasure) {
        super(p, xPos, yPos, player, treasure, 20);

        width = 45;
        height = 55;
        dx = (xPos < 0) ? 6 : -6;
        dy = 0;
        hp = 50;
        maxHp = hp;

        monsterImageLeft = ImageManager.loadImage("images/ghost.png");
        monsterImageRight = ImageManager.loadImage("images/ghost.png");
    }

    @Override
    public void playDeathSound() {
        soundManager.playClip("die", false);
    }

    @Override
    public boolean isImmuneToFreeze() {
        return true;
    }

    @Override
    public boolean isImmuneToElectricity() {
        return true;
    }

    @Override
    public boolean isImmuneToFire() {
        return true;
    }

    @Override
    public boolean isResistantToSprit() {
        return false;
    }

    @Override
    public boolean isImmuneToExplosive() {
        return true;
    }

    @Override
    protected void collideWithPlayer() {
    }
}
