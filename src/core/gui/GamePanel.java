package core.gui;

import core.model.*;
import core.persistence.ReplayLogger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Panel that displays the active game.
 * Uses a single background thread to advance turns.
 */
public class GamePanel extends JPanel {
    private final MainFrame parent;
    private List<Player> players;
    private Game game;
    private ReplayLogger logger;
    private boolean isPvP;

    // UI constants
    private static final int MAX_FACE_UP_CARDS   = 4;
    private static final int PILE_OFFSET         = 10;
    private static final int CARD_WIDTH          = 100;
    private static final int CARD_HEIGHT         = 150;
    private static final int MAX_AI_CARD_DISPLAY = 5;
    private static final String BACK_IMAGE_PATH  = Card.backImagePath();

    // Panels and controls
    private final JPanel aiHandsPanel;
    private final JPanel tablePanel;
    private final JPanel playersPanel;
    private final JPanel playerHandPanel;
    private final JScrollPane playerScrollPane;
    private final JPanel controlsPanel;
    private final JPanel pveSouthPanel;

    // Buttons
    private final JButton purgeBtn;
    private final JButton sortRankBtn;
    private final JButton sortSuitBtn;
    private final JButton sortColorBtn;
    private final JButton endTurnBtn;

    public GamePanel(MainFrame parent) {
        super(new BorderLayout(10, 10));
        this.parent = parent;

        // Initialize panels
        aiHandsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        aiHandsPanel.setBorder(BorderFactory.createTitledBorder("Opponents"));

        tablePanel = new JPanel(new FlowLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Purged Pairs"));

        playersPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        // no titled border for PvP players panel

        // Player hand panel
        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        playerScrollPane = createCenteredScrollPane(playerHandPanel);
        playerScrollPane.setBorder(BorderFactory.createTitledBorder("Your Hand"));

        // Controls panel
        purgeBtn     = new SoundButton("Purge Pairs");
        sortRankBtn  = new SoundButton("Sort by Rank");
        sortSuitBtn  = new SoundButton("Sort by Suit");
        sortColorBtn = new SoundButton("Sort by Color");
        endTurnBtn   = new SoundButton("End Turn");

        controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlsPanel.add(purgeBtn);
        controlsPanel.add(sortRankBtn);
        controlsPanel.add(sortSuitBtn);
        controlsPanel.add(sortColorBtn);
        controlsPanel.add(endTurnBtn);

        // PvE bottom layout
        pveSouthPanel = new JPanel();
        pveSouthPanel.setLayout(new BoxLayout(pveSouthPanel, BoxLayout.Y_AXIS));
        pveSouthPanel.add(playerScrollPane);
        pveSouthPanel.add(controlsPanel);

        // Setup HumanStrategy callbacks
        HumanStrategy.setRefreshCallback(() -> SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
        }));
        HumanStrategy.registerButtons(
                purgeBtn, sortRankBtn, sortSuitBtn, sortColorBtn, endTurnBtn
        );
        purgeBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
        }));
    }

    /**
     * Wraps a panel in a BoxLayout container with horizontal glue to center its contents.
     */
    private JScrollPane createCenteredScrollPane(JPanel innerPanel) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(innerPanel);
        wrapper.add(Box.createHorizontalGlue());
        JScrollPane scroll = new JScrollPane(
                wrapper,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Scale an icon to CARD_WIDTH×CARD_HEIGHT */
    private ImageIcon scaledIcon(String pathOrUrl) {
        ImageIcon icon;
        try {
            // Try interpreting the string as a URL (e.g. "file:/…" or "jar:…")
            URL url = new URL(pathOrUrl);
            icon = new ImageIcon(url);
        } catch (Exception ex) {
            // Fallback: treat as a normal filename
            icon = new ImageIcon(pathOrUrl);
        }
        Image img = icon.getImage()
                .getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }


    /** Enable or disable controls based on the current player */
    private void updateControls() {
        Player curr = players.get(game.getCurrentIndex());
        boolean human = curr.getStrategy() instanceof HumanStrategy;
        purgeBtn.setEnabled(human && curr.hasPairs());
        sortRankBtn.setEnabled(human);
        sortSuitBtn.setEnabled(human);
        sortColorBtn.setEnabled(human);
        endTurnBtn.setEnabled(human);
    }

    /** Refresh all UI panels based on mode */
    private void refreshUI() {
        if (isPvP) {
            // Purged Pairs at top
            tablePanel.removeAll();
            List<Card> allPairs = game.getAllTablePairs();
            int totalPairs = allPairs.size() / 2;
            int startIndex = Math.max(0, allPairs.size() - MAX_FACE_UP_CARDS);
            for (Card c : allPairs.subList(startIndex, allPairs.size())) {
                tablePanel.add(new JLabel(scaledIcon(c.imagePath())));
            }
            JLayeredPane pilePane = new JLayeredPane();
            int pileWidth  = CARD_WIDTH + PILE_OFFSET * (totalPairs - 1);
            int pileHeight = CARD_HEIGHT;
            pilePane.setPreferredSize(new Dimension(pileWidth, pileHeight));
            for (int i = 0; i < totalPairs; i++) {
                JLabel backLabel = new JLabel(scaledIcon(BACK_IMAGE_PATH));
                backLabel.setBounds(i * PILE_OFFSET, 0, CARD_WIDTH, CARD_HEIGHT);
                pilePane.add(backLabel, Integer.valueOf(i));
            }
            tablePanel.add(pilePane);
            tablePanel.revalidate();
            tablePanel.repaint();

            // Players below with scrollable, centered hands
            playersPanel.removeAll();
            int currIndex = game.getCurrentIndex();
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                List<Card> hand = p.getHand();
                if (hand != null) {
                    for (Card c : hand) {
                        handPanel.add(new JLabel(scaledIcon(c.imagePath())));
                    }
                }
                JScrollPane scroll = createCenteredScrollPane(handPanel);
                TitledBorder border = BorderFactory.createTitledBorder(p.getName());
                if (i == currIndex) border.setTitleColor(Color.RED);
                scroll.setBorder(border);
                playersPanel.add(scroll);
            }
            playersPanel.revalidate();
            playersPanel.repaint();
        } else {
            // PvE layout
            aiHandsPanel.removeAll();
            for (Player p : players) {
                if (p.getStrategy() instanceof HumanStrategy) continue;
                JPanel aiPanel = new JPanel(new FlowLayout());
                aiPanel.setBorder(BorderFactory.createTitledBorder(p.getName()));
                List<Card> hand = p.getHand();
                if (hand != null) {
                    int count = Math.min(hand.size(), MAX_AI_CARD_DISPLAY);
                    for (int j = 0; j < count; j++)
                        aiPanel.add(new JLabel(scaledIcon(BACK_IMAGE_PATH)));
                    if (hand.size() > count)
                        aiPanel.add(new JLabel("+" + (hand.size() - count)));
                }
                aiHandsPanel.add(aiPanel);
            }
            aiHandsPanel.revalidate();
            aiHandsPanel.repaint();

            tablePanel.removeAll();
            List<Card> allPairs = game.getAllTablePairs();
            int totalPairs = allPairs.size() / 2;
            int startIndex = Math.max(0, allPairs.size() - MAX_FACE_UP_CARDS);
            for (Card c : allPairs.subList(startIndex, allPairs.size()))
                tablePanel.add(new JLabel(scaledIcon(c.imagePath())));
            JLayeredPane pilePane = new JLayeredPane();
            int pileWidth  = CARD_WIDTH + PILE_OFFSET * (totalPairs - 1);
            int pileHeight = CARD_HEIGHT;
            pilePane.setPreferredSize(new Dimension(pileWidth, pileHeight));
            for (int i = 0; i < totalPairs; i++) {
                JLabel backLabel = new JLabel(scaledIcon(BACK_IMAGE_PATH));
                backLabel.setBounds(i * PILE_OFFSET, 0, CARD_WIDTH, CARD_HEIGHT);
                pilePane.add(backLabel, Integer.valueOf(i));
            }
            tablePanel.add(pilePane);
            tablePanel.revalidate();
            tablePanel.repaint();

            playerHandPanel.removeAll();
            Optional<Player> humanOpt = players.stream()
                    .filter(p -> p.getStrategy() instanceof HumanStrategy)
                    .findFirst();
            if (humanOpt.isPresent()) {
                List<Card> hand = humanOpt.get().getHand();
                if (hand != null)
                    for (Card c : hand)
                        playerHandPanel.add(new JLabel(scaledIcon(c.imagePath())));
            }
            playerHandPanel.revalidate();
            playerHandPanel.repaint();
        }
    }

    /** Start a new player vs player game */
    public void startNewPlayerGame(List<Player> players) {
        this.players = players;
        this.isPvP = true;
        removeAll();
        add(tablePanel, BorderLayout.NORTH);
        add(playersPanel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.SOUTH);
        revalidate(); repaint();

        Deck deck = new Deck();
        deck.shuffle();
        var dealt = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setHand(dealt.get(i));
        }
        try {
            logger = new ReplayLogger();
            game   = new Game(players, false, logger);
        } catch (IOException ex) {
            game   = new Game(players, true, null);
        }
        game.start();
        SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
            Thread gameLoop = new Thread(() -> {
                while (game.nextTurn()) {
                    SwingUtilities.invokeLater(() -> {
                        refreshUI();
                        updateControls();
                    });
                    Player curr = players.get(game.getCurrentIndex());
                    if (!(curr.getStrategy() instanceof HumanStrategy)) {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    }
                }
                SwingUtilities.invokeLater(this::onGameEnd);
            }, "Game-Loop");
            gameLoop.setDaemon(true);
            gameLoop.start();
        });
    }

    /** Start a new bot-only game */
    public void startNewBotGame(List<Player> players) {
        this.players = players;
        this.isPvP = false;
        removeAll();
        add(aiHandsPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(pveSouthPanel, BorderLayout.SOUTH);
        revalidate(); repaint();

        Deck deck = new Deck();
        deck.shuffle();
        var dealt = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setHand(dealt.get(i));
        }
        try {
            logger = new ReplayLogger();
            game   = new Game(players, false, logger);
        } catch (IOException ex) {
            game   = new Game(players, true, null);
        }
        game.start();
        SwingUtilities.invokeLater(() -> {
            refreshUI();
            updateControls();
        });
        Thread gameLoop = new Thread(() -> {
            while (game.nextTurn()) {
                SwingUtilities.invokeLater(() -> {
                    refreshUI();
                    updateControls();
                });
                Player moved = players.get(game.getCurrentIndex());
                if (!(moved.getStrategy() instanceof HumanStrategy)) {
                    try { Thread.sleep(3); } catch (InterruptedException ignored) {}
                }
            }
            SwingUtilities.invokeLater(this::onGameEnd);
        }, "Game-Loop");
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    /** Handle end of game */
    private void onGameEnd() {
        SoundManager.playNegative();
        try {
            if (logger != null) logger.close();
        } catch (IOException ignored) {}
        var loser = game.getLoser().orElse(null);
        if (loser != null) parent.getScoreboard().recordLoss(loser.getName());
        JOptionPane.showMessageDialog(this,
                "Game Over! Loser: " + (loser != null ? loser.getName() : "?"));
        parent.showMenu();
    }
}