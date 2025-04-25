package core.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Represents a playing card, defined by its rank, suit, and
 * the path to its image file for the UI.
 *
 * @param rank      the rank of the card (ACE, TWO, …, KING)
 * @param suit      the suit of the card (CLUBS, DIAMONDS, HEARTS, SPADES)
 * @param imagePath the resource path to the card's image (non-null)
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
     * Returns absolute path of the card image
     *
     * @return a String representing the path of the image
     * @throws IOException if is image not found
     */
    public String imagePath() {
        try {
            File image = new File(imagePath);
            if(image.exists() && !image.isDirectory()) {
                return image.getAbsolutePath();
            }
            else {
                throw new IOException("Card image not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A human-readable representation, e.g. "ACE of SPADES".
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
