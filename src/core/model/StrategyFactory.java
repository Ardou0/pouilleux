package core.model;

import java.util.List;
import java.util.Random;

/**
 * Utility to assign each bot one of several different strategies at random.
 */
public final class StrategyFactory {
    private static final List<MoveStrategy> AVAILABLE = List.of(
            new DrawThenPurgeStrategy(),
            new MixedRandomStrategy()
    );
    private static final Random RNG = new Random();

    /** @return a randomly selected MoveStrategy from the pool. */
    public static MoveStrategy randomStrategy() {
        return AVAILABLE.get(RNG.nextInt(AVAILABLE.size()));
    }
}
