// src/main/java/core/model/Player.java
package core.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Represents one player in the Pouilleux game,
 * delegating turn logic to a MoveStrategy.
 */
public class Player {
    private String name;
    private final List<Card> hand;
    private final MoveStrategy strategy;

    public Player(String name, List<Card> initialHand, MoveStrategy strategy) {
        this.name     = Objects.requireNonNull(name,        "Player name must not be null");
        this.hand     = new ArrayList<>(Objects.requireNonNull(initialHand, "Initial hand must not be null"));
        this.strategy = Objects.requireNonNull(strategy,    "MoveStrategy must not be null");
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    /** Returns an unmodifiable snapshot of current hand. */
    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    /** Overwrites this player's hand with the given list. */
    public void setHand(List<Card> newHand) {
        hand.clear();
        hand.addAll(newHand);
    }

    public MoveStrategy getStrategy() { return strategy; }
    public int getHandSize() { return hand.size(); }
    public boolean hasNoCards() { return hand.isEmpty(); }

    /**
     * @return true if at least one same-color pair exists in hand
     */
    public boolean hasPairs() {
        // same as before, works off the unmodifiable getHand()
        Map<Rank, List<Card>> byRank = getHand().stream()
                .collect(Collectors.groupingBy(Card::rank));
        for (var group : byRank.values()) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    if (group.get(i).sameColor(group.get(j))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes at most one red pair and one black pair per rank.
     * Operates on the *internal* mutable hand list.
     *
     * @return the cards removed (in pairs)
     */
    public List<Card> purgePairs() {
        // snapshot for grouping
        List<Card> snapshot = new ArrayList<>(hand);
        if (snapshot.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Rank, List<Card>> byRank = snapshot.stream()
                .collect(Collectors.groupingBy(Card::rank));

        List<Card> removed = new ArrayList<>();

        for (List<Card> group : byRank.values()) {
            boolean redRemoved   = false;
            boolean blackRemoved = false;
            for (int i = 0; i < group.size(); i++) {
                Card c1 = group.get(i);
                for (int j = i + 1; j < group.size(); j++) {
                    Card c2 = group.get(j);
                    if (c1.sameColor(c2)) {
                        boolean isRed = c1.suit() == Suit.HEARTS || c1.suit() == Suit.DIAMONDS;
                        if ((isRed && !redRemoved) || (!isRed && !blackRemoved)) {
                            removed.add(c1);
                            removed.add(c2);
                            if (isRed)   redRemoved   = true;
                            else         blackRemoved = true;
                            break;
                        }
                    }
                }
                if (redRemoved && blackRemoved) break;
            }
        }

        // remove from *this* player's hand:
        hand.removeAll(removed);
        return removed;
    }

    /**
     * Draws a random card from another player.
     */
    public Card drawFrom(Player from) {
        Objects.requireNonNull(from, "Player to draw from must not be null");
        if (from.hand.isEmpty()) {
            throw new IllegalStateException("Cannot draw from '" + from.name + "'; their hand is empty");
        }
        int idx    = ThreadLocalRandom.current().nextInt(from.hand.size());
        Card card  = from.hand.remove(idx);
        hand.add(card);
        return card;
    }

    /**
     * Puts the given cards back into this player's hand.
     * Used by strategies to “undo” unwanted purges.
     */
    public void receiveCards(List<Card> cards) {
        Objects.requireNonNull(cards, "cards must not be null");
        hand.addAll(cards);
    }

    /**
     * Executes one turn in the game. Returns the cards that were actually purged.
     */
    public List<Card> takeTurn(Player leftNeighbor) {
        Objects.requireNonNull(leftNeighbor, "Neighbor must not be null");
        return strategy.makeMove(this, leftNeighbor);
    }

    // —— Sorting helpers ——

    /** Sorts this hand by rank, then by suit. */
    public void sortHandByRank() {
        hand.sort(Comparator
                .comparing(Card::rank)
                .thenComparing(Card::suit));
    }

    /** Sorts this hand by suit, then by rank. */
    public void sortHandBySuit() {
        hand.sort(Comparator
                .comparing(Card::suit)
                .thenComparing(Card::rank));
    }

    /**
     * Sorts by color (all reds first, then blacks), within each color by rank.
     */
    public void sortHandByColor() {
        hand.sort(Comparator
                .comparing((Card c) -> (c.suit() == Suit.HEARTS || c.suit() == Suit.DIAMONDS) ? 0 : 1)
                .thenComparing(Card::rank));
    }
}
