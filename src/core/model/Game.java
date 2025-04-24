// src/main/java/core/model/Game.java
package core.model;

import java.util.*;
import core.persistence.ReplayLogger;

/**
 * Core engine with built-in replay logging of purges.
 */
public class Game {
    private final List<Player> players;
    private final boolean recordHistory;       // <— flag to enable in-memory history
    private final List<GameState> history;     // <— the in-memory log
    private final ReplayLogger replayLogger;
    private int stepCounter, currentIndex;
    private final Random rng = new Random();

    public Game(List<Player> players) {
        this(players, /* inMemory= */ true, /* fileLogger= */ null);
    }

    /**
     * @param players       list of ≥2 players
     * @param inMemory      whether to keep an in-memory history
     * @param fileLogger    if non-null, also write each step to file
     */
    public Game(List<Player> players,
                boolean inMemory,
                ReplayLogger fileLogger) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("Need at least two players");
        }
        this.players       = Objects.requireNonNull(players);
        this.recordHistory = inMemory;
        // if inMemory, we get a real ArrayList; otherwise an unmodifiable empty list
        this.history       = inMemory
                ? new ArrayList<>()
                : Collections.emptyList();
        this.replayLogger  = fileLogger; // may be null
        this.currentIndex  = rng.nextInt(players.size());
        this.stepCounter   = 0;
    }

    /** Phase 1: each player purges initial pairs—record who removed what. */
    public void start() {
        for (Player p : players) {
            List<Card> removed = p.purgePairs();
            recordState(p.getName() + " initial purge: " + removed);
        }
    }

    /**
     * One full phase-2 turn:
     *   – pick next active player
     *   – pick their next active neighbor
     *   – execute draw+purge, record who removed what
     */
    public boolean nextTurn() {
        if (isGameOver()) {
            return false;
        }

        currentIndex = findNextActive(currentIndex);
        Player current = players.get(currentIndex);

        int neighborIdx = findNextActive(currentIndex);
        if (neighborIdx == currentIndex) {
            // only one left
            recordState("Game over");
            return false;
        }
        Player left = players.get(neighborIdx);

        List<Card> removed = current.takeTurn(left);
        recordState(current.getName() + " turn purge: " + removed);

        return true;
    }

    public boolean isGameOver() {
        return players.stream().filter(p -> !p.hasNoCards()).count() <= 1;
    }

    public Optional<Player> getLoser() {
        if (!isGameOver()) return Optional.empty();
        return players.stream().filter(p -> !p.hasNoCards()).findFirst();
    }

    public List<GameState> getHistory() {
        return recordHistory
                ? List.copyOf(history)
                : Collections.emptyList();
    }

    /** logs a snapshot with a descriptive message */
    private void recordState(String desc) {
        GameState state = new GameState(
                stepCounter++,
                desc,
                players.stream()
                        .map(p -> new PlayerSnapshot(p.getName(), p.getHand()))
                        .toList()
        );
        if (recordHistory) {
            history.add(state);
        }
        if (replayLogger != null) {
            replayLogger.logState(state);
        }
    }


    private List<PlayerSnapshot> snapshotPlayers() {
        return players.stream()
                .map(p -> new PlayerSnapshot(p.getName(), p.getHand()))
                .toList();
    }

    /** finds next player index with cards, wrapping around */
    private int findNextActive(int start) {
        int n = players.size();
        for (int d = 1; d < n; d++) {
            int idx = (start + d) % n;
            if (!players.get(idx).hasNoCards()) {
                return idx;
            }
        }
        return start;
    }
}
