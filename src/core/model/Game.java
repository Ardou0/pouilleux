package core.model;

import core.gui.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import core.persistence.ReplayLogger;

/**
 * Core engine with built-in replay logging of purges.
 * Human players do NOT auto-purge in Phase 1.
 */
public class Game {
    private final List<Player> players;
    private final boolean recordHistory;
    private final List<GameState> history;
    private final ReplayLogger replayLogger;
    private int stepCounter;
    private int currentIndex;
    private final List<Card> tablePairs = new ArrayList<>();

    public Game(List<Player> players) {
        this(players, /* inMemory=*/true, /* fileLogger=*/null);
    }

    public Game(List<Player> players,
                boolean inMemory,
                ReplayLogger fileLogger) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("Need at least two players");
        }
        this.players      = List.copyOf(players);
        this.recordHistory= inMemory;
        this.history      = inMemory ? new ArrayList<>() : Collections.emptyList();
        this.replayLogger = fileLogger;
        this.stepCounter  = 0;
        this.currentIndex = -1;                   // safe until start()
    }

    /** Phase 1: bots auto-purge, humans wait for your button. */
    public void start() {
        // pick a random player to start Phase 2
        boolean areBot = false;
        int i=0;
        // Phase 1: only bots auto-purge
        for (Player p : players) {
                if(!(p.getStrategy() instanceof HumanStrategy)) {
                    areBot = true;
                }
                List<Card> removed = p.purgePairs();
                tablePairs.addAll(removed);
                recordState(p.getName() + " initial purge: " + removed);
                i++;
        }
        if(!areBot) {
            currentIndex = ThreadLocalRandom.current().nextInt(players.size());
        }
        else {
            currentIndex = 1;
        }
    }

    /**
     * One full phase‐2 turn:
     *   – move to next active player
     *   – draw from their left neighbor
     *   – purge that one
     *   – log how many, and stop when game over
     */
    public boolean nextTurn() {
        if (isGameOver()) {
            recordState("Game over");
            return false;
        }

        // advance to next with cards
        currentIndex = findNextActive(currentIndex);
        Player current = players.get(currentIndex);

        // pick their neighbor
        int neighborIdx = findNextActive(currentIndex);
        if (neighborIdx == currentIndex) {
            recordState("Game over");
            return false;
        }
        Player left = players.get(neighborIdx);

        // let them draw & purge
        List<Card> removed = current.takeTurn(left);
        tablePairs.addAll(removed);
        recordState(current.getName() + " turn purge: " + removed);
        return true;
    }

    public boolean isGameOver() {
        // 1) check "only J♠" condition
        for (Player p : players) {
            if (p.getHandSize() == 1) {
                Card only = p.getHand().get(0);
                if (only.rank() == Rank.JACK && only.suit() == Suit.SPADES) {
                    return true;
                }
            }
        }
        // 2) fallback: only one player still has any cards
        long survivors = players.stream()
                .filter(pl -> !pl.hasNoCards())
                .count();
        return survivors <= 1;
    }


    public Optional<Player> getLoser() {
        // 1) check for the Pouilleux‐only loser:
        for (Player p : players) {
            if (p.getHandSize() == 1) {
                Card only = p.getHand().get(0);
                if (only.rank() == Rank.JACK && only.suit() == Suit.SPADES) {
                    return Optional.of(p);
                }
            }
        }
        // 2) old logic: last one with cards
        if (!isGameOver()) {
            return Optional.empty();
        }
        return players.stream()
                .filter(pl -> !pl.hasNoCards())
                .findFirst();
    }


    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Card> getAllTablePairs() {
        return List.copyOf(tablePairs);
    }

    public List<GameState> getHistory() {
        return recordHistory ? List.copyOf(history) : Collections.emptyList();
    }

    /** Snapshot and optional file log */
    private void recordState(String desc) {
        GameState st = new GameState(
                stepCounter++,
                desc,
                players.stream()
                        .map(p -> new PlayerSnapshot(p.getName(), p.getHand()))
                        .toList()
        );
        if (recordHistory) history.add(st);
        if (replayLogger != null) replayLogger.logState(st);
    }

    /** wraparound to next non‐empty hand */
    private int findNextActive(int start) {
        int n = players.size();
        for (int d = 1; d <= n; d++) {
            int idx = (start + d + n) % n;
            if (!players.get(idx).hasNoCards()) {
                return idx;
            }
        }
        // fallback should never happen
        return start < 0 ? 0 : start % n;
    }
}
