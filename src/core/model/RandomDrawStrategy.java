package core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Simplest AI: draw one random card, purge nothing.
 */
public class RandomDrawStrategy implements MoveStrategy {
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(leftNeighbor);
        self.drawFrom(leftNeighbor);
        if(self.hasPairs()){
            self.purgePairs();
        }
        return Collections.emptyList();
    }
}