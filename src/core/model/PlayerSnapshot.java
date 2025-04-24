package core.model;

import java.util.List;

/**
 * Immutable snapshot of one player's name and hand at a moment in time.
 */
public record PlayerSnapshot(String playerName, List<Card> hand) {}
