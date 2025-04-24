package core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Look at the ratio of red vs black cards:
 *  1) Purge only the majority color (red or black), holding the other color as bluff
 *  2) Draw one card
 *  3) Purge any newly formed pairs (any color)
 *
 * Returns the full list of cards actually removed this turn.
 */
public class ColorAwareStrategy implements MoveStrategy {
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self,         "Player must not be null");
        Objects.requireNonNull(leftNeighbor, "Left neighbor must not be null");

        List<Card> actuallyRemoved = new ArrayList<>();

        // 1) Initial purge: remove all pairs, then split by color
        List<Card> firstPurge = self.purgePairs();
        long redCount   = firstPurge.stream().filter(c -> c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS).count();
        long blackCount = firstPurge.size() - redCount; // but this is only counting purged cards; better to inspect hand before. Let's fix:
        // Actually determine majority from current hand:
        List<Card> handSnapshot = self.getHand();
        long totalRed   = handSnapshot.stream().filter(c -> c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS).count();
        long totalBlack = handSnapshot.size() - totalRed;
        boolean purgeRed = totalRed > totalBlack;

        // Keep only the pairs of the majority color removed:
        List<Card> majorityRemoved = firstPurge.stream()
                .filter(c -> purgeRed
                        ? (c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS)
                        : (c.suit() == Suit.CLUBS   || c.suit() == Suit.SPADES))
                .collect(Collectors.toList());
        List<Card> accidental = firstPurge.stream()
                .filter(c -> !majorityRemoved.contains(c))
                .collect(Collectors.toList());

        actuallyRemoved.addAll(majorityRemoved);
        // Return the other-color cards back into the hand
        self.receiveCards(accidental);

        // 2) Draw one card
        self.drawFrom(leftNeighbor);

        // 3) Purge any new pairs (of either color)
        List<Card> secondPurge = self.purgePairs();
        actuallyRemoved.addAll(secondPurge);

        return actuallyRemoved;
    }
}
