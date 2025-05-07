package core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Simplest AI: draw one random card, purge nothing.
 */
public class RandomDrawStrategy implements MoveStrategy {
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
        self.drawFrom(leftNeighbor);
        return Collections.emptyList();
    }
}