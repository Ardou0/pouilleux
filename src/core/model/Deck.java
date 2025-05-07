package core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of playing cards (52 cards minus the Jack of Clubs),
 * each with its associated image path.
 */
public class Deck {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Constructs a new deck containing all ranks and suits,
     * except the Jack of Clubs. Image paths are generated
     * using the convention "/images/cards/{rank}_{suit}.png".
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (rank == Rank.JACK && suit == Suit.CLUBS) {
                    continue; // omit the Jack of Clubs (“pouilleux”)
                }
                String path = String.format(
                        "/images/cards/%s_%s.png",
                        rank.name().toLowerCase(),
                        suit.name().toLowerCase()
                );
                cards.add(new Card(rank, suit, path));
            }
        }
    }

    /**
     * Randomly shuffles the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals the cards to the given number of players in round-robin fashion.
     *
     * @param players the number of players (must be at least 2)
     * @return a List of hands; each hand is a List of Cards
     * @throws IllegalArgumentException if players < 2
     */
    public List<List<Card>> deal(int players) {
        if (players < 2) {
            throw new IllegalArgumentException("Number of players must be at least 2");
        }
        List<List<Card>> hands = new ArrayList<>(players);
        for (int i = 0; i < players; i++) {
            hands.add(new ArrayList<>());
        }
        for (int i = 0; i < cards.size(); i++) {
            hands.get(i % players).add(cards.get(i));
        }
        return hands;
    }

    /**
     * Returns an unmodifiable view of the current deck.
     *
     * @return the cards remaining in the deck
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}