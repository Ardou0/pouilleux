package core.model;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Randomly picks one of the three base strategies each turn.
 */
public class MixedRandomStrategy implements MoveStrategy {
    private static final List<MoveStrategy> OPTIONS = List.of(
            new RandomDrawStrategy(),
            new PurgeThenDrawStrategy(),
            new DrawThenPurgeStrategy()
    );
    private static final Random RNG = new Random();

    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(leftNeighbor);
        MoveStrategy choice = OPTIONS.get(RNG.nextInt(OPTIONS.size()));
        return choice.makeMove(self, leftNeighbor);
    }
}
