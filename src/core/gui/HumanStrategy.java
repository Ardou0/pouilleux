package core.gui;

import core.model.MoveStrategy;
import core.model.Player;
import core.model.Card;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A MoveStrategy for a human‐controlled player.
 * Blocks in makeMove() until the user enqueues END_TURN.
 * All other actions immediately invoke the UI refresh callback.
 */
public class HumanStrategy implements MoveStrategy {
    public enum Action {
        PURGE_PAIRS,
        SORT_BY_RANK,
        SORT_BY_SUIT,
        SORT_BY_COLOR,
        END_TURN
    }

    // single queue for all human actions, including END_TURN
    private static final BlockingQueue<Action> queue = new LinkedBlockingQueue<>();

    // callback to refresh the UI on EDT after any state change
    private static Runnable refreshCallback;

    /** Install a callback to be run on the EDT whenever the UI needs repainting. */
    public static void setRefreshCallback(Runnable cb) {
        refreshCallback = cb;
    }

    /**
     * Bind your buttons to the human actions.
     * Call once from your GamePanel constructor.
     */
    public static void registerButtons(
            JButton purgeBtn,
            JButton sortRankBtn,
            JButton sortSuitBtn,
            JButton sortColorBtn,
            JButton endTurnBtn
    ) {
        // helper to enqueue PURGE/SORT actions and refresh immediately
        ActionListener enqueueAndRefresh = e -> {
            String txt = ((JButton) e.getSource()).getText();
            Action act = switch (txt) {
                case "Purge Pairs"   -> Action.PURGE_PAIRS;
                case "Sort by Rank"  -> Action.SORT_BY_RANK;
                case "Sort by Suit"  -> Action.SORT_BY_SUIT;
                case "Sort by Color" -> Action.SORT_BY_COLOR;
                default               -> null;
            };
            if (act != null) {
                queue.offer(act);
                if (refreshCallback != null) {
                    SwingUtilities.invokeLater(refreshCallback);
                }
            }
        };

        purgeBtn.addActionListener(enqueueAndRefresh);
        sortRankBtn.addActionListener(enqueueAndRefresh);
        sortSuitBtn.addActionListener(enqueueAndRefresh);
        sortColorBtn.addActionListener(enqueueAndRefresh);

        // END_TURN simply enqueues that action; makeMove() will see it and return
        endTurnBtn.addActionListener(e -> queue.offer(Action.END_TURN));
    }

    /**
     * Blocks until the user clicks End Turn, processing any number
     * of PURGE / SORT actions beforehand.  Returns the cards purged.
     */
    @Override
    public List<Card> makeMove(Player self, Player leftNeighbor) {
        List<Card> removed = new ArrayList<>();
        try {
            while (true) {
                Action action = queue.take();
                switch (action) {
                    case PURGE_PAIRS -> removed.addAll(self.purgePairs());
                    case SORT_BY_RANK -> self.sortHandByRank();
                    case SORT_BY_SUIT -> self.sortHandBySuit();
                    case SORT_BY_COLOR -> self.sortHandByColor();
                    case END_TURN -> {
                        // perform the draw and finish the turn
                        if (!self.hasNoCards()) {
                            self.drawFrom(leftNeighbor);
                        }
                        return removed;
                    }
                }
                // immediately repaint after any PURGE/SORT
                if (refreshCallback != null) {
                    SwingUtilities.invokeLater(refreshCallback);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return removed;
        }
    }
}
