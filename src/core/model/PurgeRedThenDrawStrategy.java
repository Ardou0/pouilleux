package core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 1) Remove only red pairs (♥+♦), holding black pairs as bluff.
 * 2) Draw one random card.
 * 3) Remove only black pairs (♣+♠) newly formed by that draw.
 * Returns the combined list of cards actually purged this turn.
 */
public class PurgeRedThenDrawStrategy implements MoveStrategy {
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        Objects.requireNonNull(self,         "Player must not be null");
        Objects.requireNonNull(leftNeighbor, "Left neighbor must not be null");

        List<Card> actuallyRemoved = new ArrayList<>();

        // 1) Purge all pairs, then split into red vs black
        List<Card> initialPurge = self.purgePairs();
        List<Card> redPurge = initialPurge.stream()
                .filter(c -> c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS)
                .collect(Collectors.toList());
        List<Card> blackAccidental = initialPurge.stream()
                .filter(c -> c.suit() == Suit.CLUBS || c.suit() == Suit.SPADES)
                .collect(Collectors.toList());

        // We only want to keep the reds out
        actuallyRemoved.addAll(redPurge);
        // Return the black ones to the hand
        self.receiveCards(blackAccidental);

        // 2) Draw one random card
        self.drawFrom(leftNeighbor);

        // 3) Purge again, but only keep black pairs now
        List<Card> secondPurge = self.purgePairs();
        List<Card> blackPurge = secondPurge.stream()
                .filter(c -> c.suit() == Suit.CLUBS || c.suit() == Suit.SPADES)
                .collect(Collectors.toList());
        actuallyRemoved.addAll(blackPurge);

        // Return any red pairs accidentally purged in the second sweep
        List<Card> redAccidental = secondPurge.stream()
                .filter(c -> c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS)
                .collect(Collectors.toList());
        self.receiveCards(redAccidental);

        return actuallyRemoved;
    }
}
