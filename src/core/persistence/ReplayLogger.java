// src/main/java/core/persistence/ReplayLogger.java
package core.persistence;

import core.model.GameState;
import core.model.PlayerSnapshot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Logs each GameState to a per-game text file named:
 *   replays/replay_game_{n}_{timestamp}.log
 *
 * Automatically picks n = (max existing n) + 1.
 */
public class ReplayLogger implements AutoCloseable {
    private static final Path DIR = Paths.get("replays");
    private static final Pattern FILENAME_REGEX =
            Pattern.compile("^replay_game_(\\d+)_.*\\.log$");
    private final BufferedWriter writer;
    private final DateTimeFormatter timeFmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReplayLogger() throws IOException {
        // ensure directory exists
        Files.createDirectories(DIR);

        // scan for existing files, extract their game numbers
        int nextGame = Files.list(DIR)
                .map(Path::getFileName)
                .map(Path::toString)
                .map(FILENAME_REGEX::matcher)
                .filter(Matcher::matches)
                .map(m -> Integer.parseInt(m.group(1)))
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;

        // build our filename
        String ts   = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path file   = DIR.resolve(String.format("replay_game_%d_%s.log", nextGame, ts));

        writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        writer.write("Pouilleux Replay Log — game " + nextGame
                + " started at " + LocalDateTime.now().format(timeFmt));
        writer.newLine();
        writer.newLine();
    }

    /** Append one GameState to the file in human-readable form. */
    public void logState(GameState state) {
        try {
            writer.write(String.format("STEP %d: %s", state.step(), state.description()));
            writer.newLine();
            for (PlayerSnapshot snap : state.playerSnapshots()) {
                writer.write("  " + snap.playerName() + " → " + snap.hand());
                writer.newLine();
            }
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Warning: failed to write replay: " + e.getMessage());
        }
    }

    public static void clearAll() {
        try {
            // Ensure directory exists
            if (!Files.exists(DIR)) return;

            // List and delete each file
            try (Stream<Path> files = Files.list(DIR)) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Warning: could not delete replay file "
                                + path.getFileName() + ": " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Warning: failed to clear replay directory: "
                    + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        writer.write("End of replay at " + LocalDateTime.now().format(timeFmt));
        writer.newLine();
        writer.close();
    }
}
