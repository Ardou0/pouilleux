package core.model;

import java.util.List;
import java.util.Objects;

/**
 * Draw one card, then purge newly formed pairs.
 */
public class DrawThenPurgeStrategy implements MoveStrategy {
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(leftNeighbor);
        self.drawFrom(leftNeighbor);
        return self.purgePairs();
    }
}