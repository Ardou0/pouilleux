package core.model;

import java.net.URL;
import java.util.Objects;

/**
 * Represents a playing card, defined by its rank, suit, and
 * the resource path to its image file for the UI.
 *
 * @param rank      the rank of the card (ACE, TWO, …, KING)
 * @param suit      the suit of the card (CLUBS, DIAMONDS, HEARTS, SPADES)
 * @param imagePath the classpath resource path to the card's image (e.g. "/images/cards/ACE_SPADES.png")
 */
public record Card(Rank rank, Suit suit, String imagePath) {

    public Card {
        Objects.requireNonNull(rank,      "Card rank must not be null");
        Objects.requireNonNull(suit,      "Card suit must not be null");
        Objects.requireNonNull(imagePath, "Card imagePath must not be null");
    }

    /**
     * Returns whether this card and the other share the same color
     * (both red or both black).
     *
     * @param other the card to compare with
     * @return true if both cards are red or both are black
     * @throws NullPointerException if other is null
     */
    public boolean sameColor(Card other) {
        Objects.requireNonNull(other, "Compared card must not be null");
        boolean thisRed  = suit == Suit.HEARTS  || suit == Suit.DIAMONDS;
        boolean otherRed = other.suit() == Suit.HEARTS || other.suit() == Suit.DIAMONDS;
        return thisRed == otherRed;
    }

    /**
     * Returns a URL string to the card image resource on the classpath.
     *
     * @return a String URL for loading the image via ImageIcon
     * @throws RuntimeException if the resource is not found
     */
    public String imagePath() {
        URL resource = Card.class.getResource(imagePath);
        if (resource == null) {
            throw new RuntimeException("Card image resource not found: " + imagePath);
        }
        return resource.toExternalForm();
    }

    /**
     * Returns a URL string to the back-of-card image on the classpath.
     *
     * @return a String URL for loading the back image via ImageIcon
     * @throws RuntimeException if the resource is not found
     */
    public static String backImagePath() {
        String backPath = "/images/cards/BACK_CARD.png";
        URL resource = Card.class.getResource(backPath);
        if (resource == null) {
            throw new RuntimeException("Back card image resource not found: " + backPath);
        }
        return resource.toExternalForm();
    }

    /**
     * A human-readable representation, e.g. "ACE of SPADES".
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
