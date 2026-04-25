import java.util.List;

public class SpawnData {
        public final int wave;
        public final int level;
        public final List<Class<? extends Monster>> monsterTypes;

        public SpawnData(int wave, int level, List<Class<? extends Monster>> monsterTypes) {
            this.wave         = wave;
            this.level        = level;
            this.monsterTypes = monsterTypes;
        }
    }