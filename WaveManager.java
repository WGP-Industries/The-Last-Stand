import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {

    private static final int TOTAL_WAVES = 15;

    private int currentWave = 0;
    private int currentLevel = 0;
    private final Random random = new Random();

    private final List<Class<? extends Monster>> unlockedMonsters = new ArrayList<>();
    private final List<BulletType> unlockedBullets = new ArrayList<>();

    public int getCurrentWave()  { return currentWave; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isFinished()  { return currentWave >= TOTAL_WAVES; }

    public List<BulletType> getUnlockedBullets() { return unlockedBullets; }

    public void reset() {
        currentWave = 0;
        currentLevel = 0;
        unlockedMonsters.clear();
        unlockedBullets.clear();
    }

    private void unlockMonstersForWave(int wave) {
        switch (wave) {
            case 1  -> { 
                       unlockedMonsters.add(Ghost.class);
                        unlockedMonsters.add(Snake.class); }
                    
            case 3  ->   unlockedMonsters.add(ShadowWalker.class);
            case 4  ->   unlockedMonsters.add(ArmoredTurtle.class);
            case 5  ->   unlockedMonsters.add(FireImp.class);
            case 6  ->   unlockedMonsters.add(SplitSlime.class);
            case 8  ->   unlockedMonsters.add(Healer.class);
            case 12 ->   unlockedMonsters.add(ShieldGuardian.class);
            case 15 ->   unlockedMonsters.add(BerserkerOrc.class);
        }
    }

    private void unlockBulletsForLevel(int level) {
        switch (level) {
            case 1 -> { unlockedBullets.add(BulletType.BASIC);
                       unlockedBullets.add(BulletType.FIRE);
                          
                      }
             
                    
            case 2 -> { unlockedBullets.add(BulletType.FREEZE);
                        unlockedBullets.add(BulletType.ELECTRIC); }
            case 3 -> { unlockedBullets.add(BulletType.SPIRIT);
                        unlockedBullets.add(BulletType.EXPLOSIVE); }
            case 4 -> { unlockedBullets.add(BulletType.PIERCING);
                        unlockedBullets.add(BulletType.RAPID); }
            case 5 ->   unlockedBullets.add(BulletType.TELEPORT);
        }
    }

    private int getWaveSize() {
        if (currentWave <= 3)  return random.nextInt(4,6)+ 1 ;
        if (currentWave <= 6)  return random.nextInt(4,7) + 2;
        if (currentWave <= 9)  return random.nextInt(4,8) + 3;
        if (currentWave <= 12) return random.nextInt(4, 9) + 4;
        return random.nextInt(4) + 5;
    }

    public SpawnData nextWave() {
        currentWave++;

        int newLevel = (int) Math.ceil((double) currentWave / 3);
        if (newLevel != currentLevel) {
            currentLevel = newLevel;
            unlockBulletsForLevel(currentLevel);
        }

        unlockMonstersForWave(currentWave);

        int size = getWaveSize();
        List<Class<? extends Monster>> pool = new ArrayList<>(unlockedMonsters);
        List<Class<? extends Monster>> selected = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Class<? extends Monster> pick = pool.get(random.nextInt(pool.size()));

            boolean pickedHealer   = pick == Healer.class;
            boolean noNonHealerYet = selected.stream().noneMatch(c -> c != Healer.class);

            if (pickedHealer && noNonHealerYet) {
                List<Class<? extends Monster>> nonHealers = pool.stream()
                    .filter(c -> c != Healer.class)
                    .toList();
                if (!nonHealers.isEmpty()) {
                    selected.add(nonHealers.get(random.nextInt(nonHealers.size())));
                    continue;
                }
            }

            selected.add(pick);
        }

        return new SpawnData(currentWave, currentLevel, selected);
    }
    }