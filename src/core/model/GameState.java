package core.model;

import java.util.List;

/**
 * A single moment in the game: a step number, a human‐readable
 * description, and the list of all players' snapshots at that moment.
 */
public record GameState(
        int step,
        String description,
        List<PlayerSnapshot> playerSnapshots
) {}