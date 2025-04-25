package core.gui;

import core.model.*;
import core.persistence.ReplayLogger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Panel that displays the active game.
 * Uses a single background thread to advance turns.
 */
public class GamePanel extends JPanel {
    private final MainFrame parent;
    private List<Player> players;
    private Game game;
    private ReplayLogger logger;

    // UI
    private final JPanel handsPanel;
    private final JPanel tablePanel;
    private final JButton purgeBtn, sortRankBtn, sortSuitBtn, sortColorBtn, endTurnBtn;

    public GamePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10,10));

        // -- Table Pairs --
        tablePanel = new JPanel(new FlowLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Table Pairs"));
        add(tablePanel, BorderLayout.NORTH);

        // -- Hands --
        handsPanel = new JPanel(new GridLayout(2,2,10,10));
        add(handsPanel, BorderLayout.CENTER);

        // -- Controls --
        JPanel ctl = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
        purgeBtn       = new JButton("Purge Pairs");
        sortRankBtn    = new JButton("Sort by Rank");
        sortSuitBtn    = new JButton("Sort by Suit");
        sortColorBtn   = new JButton("Sort by Color");
        endTurnBtn     = new JButton("End Turn");
        ctl.add(purgeBtn);
        ctl.add(sortRankBtn);
        ctl.add(sortSuitBtn);
        ctl.add(sortColorBtn);
        ctl.add(endTurnBtn);
        add(ctl, BorderLayout.SOUTH);

        // wire human
        HumanStrategy.setRefreshCallback(() -> SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
        }));
        HumanStrategy.registerButtons(
                purgeBtn, sortRankBtn, sortSuitBtn, sortColorBtn, endTurnBtn
        );
    }

    /** Call this from MainFrame.showSetup → startGame(...) */
    public void startNewPlayerGame(List<Player> players) {
        this.players = players;

        // 1) deal
        Deck deck = new Deck();
        deck.shuffle();
        var dealt = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setHand(dealt.get(i));
        }

        // 2) set up model & logger
        try {
            logger = new ReplayLogger();
            game   = new Game(players, false, logger);
        } catch (IOException ex) {
            game   = new Game(players, true, null);
        }

        // 3) initial purge
        game.start();
        refreshUI();
        updateControls();

        // 4) background thread to drive turns
        Thread gameLoop = new Thread(() -> {
            while (game.nextTurn()) {
                SwingUtilities.invokeLater(() -> {
                    refreshUI();
                    updateControls();
                });
                // if next player was AI, give them a little pause
                Player curr = players.get(game.getCurrentIndex());
                if (!(curr.getStrategy() instanceof HumanStrategy)) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
            }
            SwingUtilities.invokeLater(this::onGameEnd);
        }, "Game-Loop");
        gameLoop.setDaemon(true);
        gameLoop.start();

    }

    public void startNewBotGame(List<Player> players) {
        this.players = players;

        // 1) deal exactly as before…
        Deck deck = new Deck();
        deck.shuffle();
        var dealt = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setHand(dealt.get(i));
        }

        // 2) model & logger
        try {
            logger = new ReplayLogger();
            game   = new Game(players, false, logger);
        } catch (IOException ex) {
            game   = new Game(players, true, null);
        }

        // 3) initial purge
        game.start();

        // 5) push initial UI
        SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
        });

        // 6) background loop
        Thread gameLoop = new Thread(() -> {
            while (game.nextTurn()) {
                // repaint
                SwingUtilities.invokeLater(() -> {
                    refreshUI();
                    updateControls();
                });

                // if that move was done by a bot, pause briefly
                Player moved = players.get(game.getCurrentIndex());
                if (!(moved.getStrategy() instanceof HumanStrategy)) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
            }
            // final dialog
            SwingUtilities.invokeLater(this::onGameEnd);
        }, "Game-Loop");

        gameLoop.setDaemon(true);
        gameLoop.start();
    }



    private void updateControls() {
        Player curr = players.get(game.getCurrentIndex());
        boolean human = curr.getStrategy() instanceof HumanStrategy;
        purgeBtn.setEnabled(human && curr.hasPairs());
        sortRankBtn.setEnabled(human);
        sortSuitBtn.setEnabled(human);
        sortColorBtn.setEnabled(human);
        endTurnBtn.setEnabled(human);
    }

    private void refreshUI() {
        // table
        tablePanel.removeAll();
        for (Card c : game.getAllTablePairs()) {
            tablePanel.add(new JLabel(new ImageIcon(c.imagePath())));
        }
        tablePanel.revalidate();
        tablePanel.repaint();

        // hands
        handsPanel.removeAll();
        for (Player p : players) {
            JPanel h = new JPanel(new FlowLayout());
            h.setBorder(BorderFactory.createTitledBorder(p.getName()));
            var hand = p.getHand();
            if (hand != null) for (Card c : hand) {
                h.add(new JLabel(new ImageIcon(c.imagePath())));
            }
            handsPanel.add(h);
        }
        handsPanel.revalidate();
        handsPanel.repaint();
    }

    private void onGameEnd() {
        try { if (logger != null) logger.close(); }
        catch (IOException ignored) {}
        var loser = game.getLoser().orElse(null);
        if (loser != null) parent.getScoreboard().recordLoss(loser.getName());
        JOptionPane.showMessageDialog(this,
                "Game Over! Loser: " + (loser != null ? loser.getName() : "?"));
        parent.showMenu();
    }
}
