package core.model;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Draw one card, then purge newly formed pairs.
 */
public class DrawThenPurgeStrategy implements MoveStrategy {
    private static final Random RNG = new Random();
    private static final List<Consumer<Player>> SORT_OPTIONS = List.of(
            Player::sortHandByRank,
            Player::sortHandByColor,
            Player::sortHandBySuit
    );

    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(leftNeighbor);
        Consumer<Player> sortStrategy = SORT_OPTIONS.get(RNG.nextInt(SORT_OPTIONS.size()));
        sortStrategy.accept(self);
        self.drawFrom(leftNeighbor);
        return self.purgePairs();
    }
}