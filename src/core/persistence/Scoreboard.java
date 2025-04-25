// src/main/java/core/persistence/Scoreboard.java
package core.persistence;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks and persists loss counts per player across games
 * using a simple Java Properties file.
 */
public class Scoreboard {
    private static final Path FILE = Paths.get("scores.properties");
    private final Properties props = new Properties();
    private final Map<String,Integer> losses = new HashMap<>();

    public Scoreboard() {
        load();
    }

    /**
     * Record one loss for the given player name.
     * Immediately saves to disk.
     */
    public void recordLoss(String playerName) {
        Objects.requireNonNull(playerName, "playerName must not be null");
        int newCount = losses.getOrDefault(playerName, 0) + 1;
        losses.put(playerName, newCount);
        props.setProperty(playerName, Integer.toString(newCount));
        save();
    }

    /**
     * @return a list of entries sorted descending by loss count
     */
    public List<ScoreEntry> standings() {
        return losses.entrySet().stream()
                .map(e -> new ScoreEntry(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(ScoreEntry::losses).reversed())
                .collect(Collectors.toList());
    }

    // ——— Internal persistence ——— //

    private void load() {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                props.load(in);
                for (String name : props.stringPropertyNames()) {
                    String val = props.getProperty(name);
                    try {
                        losses.put(name, Integer.parseInt(val));
                    } catch (NumberFormatException ignore) {
                        // skip invalid entries
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: could not load scoreboard: " + e.getMessage());
            }
        }
    }

    /**
     * Save the score into the scoreboard file
     *
     * @throws IOException if save went wrong
     */
    private void save() {
        // ensure parent directory
        try {
            Path parent = FILE.getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException ignored) {}

        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "Pouilleux loss count per player");
        } catch (IOException e) {
            System.err.println("Error: could not save scoreboard: " + e.getMessage());
        }
    }

    /**
     * Delete all scoreboard file to clear the persistent score
     *
     * @throws IOException if delete went wrong
     */
    public void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException e) {
            System.err.println("Error: could not delete scoreboard: " + e.getMessage());
        }
    }
}