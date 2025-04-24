import core.model.*;
import core.persistence.*;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Scoreboard board = new Scoreboard();
        // Play two games back to back
        for (int i = 0; i < 10; i++) {
            // Open a new per-game replay file
            try (ReplayLogger logger = new ReplayLogger()) {
                // 1) Deal
                Deck deck = new Deck();
                deck.shuffle();
                List<List<Card>> dealt = deck.deal(4);

                // 2) Create four players with random strategies
                List<Player> players = List.of(
                        new Player("Alice", dealt.get(0), StrategyFactory.randomStrategy()),
                        new Player("Bot1",  dealt.get(1), StrategyFactory.randomStrategy()),
                        new Player("Bot2",  dealt.get(2), StrategyFactory.randomStrategy()),
                        new Player("Bot3",  dealt.get(3), StrategyFactory.randomStrategy())
                );

                // 3) Run the game (no in-memory history, only file logging)
                Game game = new Game(players, /* inMemory= */ false, logger);
                game.start();
                while (game.nextTurn()) {
                    // just loop until over
                }

                // 4) Record the loser
                Player loser = game.getLoser().orElseThrow();
                System.out.println("Loser: " + loser.getName());
                board.recordLoss(loser.getName());

            } catch (IOException e) {
                System.err.println("Could not create replay file: " + e.getMessage());
            }
        }

        // Finally, print out the cumulative scoreboard
        for (ScoreEntry e : board.standings()) {
            System.out.printf("%s → %d losses%n", e.name(), e.losses());
        }
    }
}
