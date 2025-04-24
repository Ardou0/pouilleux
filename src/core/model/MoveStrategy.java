package core.model;

import java.util.List;

/**
 * Encapsulates the “take your turn” logic for a player.
 * Returns the list of cards purged during that move.
 */
public interface MoveStrategy {
    /**
     * Execute this player’s entire turn:
     *   1) draw (or not) from left neighbor
     *   2) purge pairs
     *
     * @param self         the active player
     * @param leftNeighbor the player to draw from
     * @return the list of cards removed (in pairs) during this turn
     */
    List<Card> makeMove(Player self, Player leftNeighbor);
}
