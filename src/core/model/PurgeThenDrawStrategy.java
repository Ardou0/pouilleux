package core.model;

import java.util.List;
import java.util.Objects;

/**
 * Purge all pairs first, then draw one card.
 */
public class PurgeThenDrawStrategy implements MoveStrategy {
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(leftNeighbor);
        List<Card> removed = self.purgePairs();
        self.drawFrom(leftNeighbor);
        return removed;
    }
}