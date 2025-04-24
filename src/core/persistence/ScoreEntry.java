package core.persistence;

/**
 * One player’s cumulative record of losses.
 */
public record ScoreEntry(String name, int losses) {}