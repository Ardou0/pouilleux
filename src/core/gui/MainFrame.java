package core.gui;

import core.model.*;
import core.persistence.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

/**
 * Main application window for Pouilleux game.
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private MenuPanel menuPanel;
    private SetupPanel setupPanel;
    private GamePanel gamePanel;
    private ScoreboardPanel scoreboardPanel;
    private ReplayPanel replayPanel;

    private Scoreboard scoreboard;

    public MainFrame() {
        super("Pouilleux");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        scoreboard = new Scoreboard();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // initialize panels
        menuPanel = new MenuPanel(this);
        setupPanel = new SetupPanel(this);
        gamePanel = new GamePanel(this);
        scoreboardPanel = new ScoreboardPanel(this);
        replayPanel = new ReplayPanel(this);

        // add to main panel
        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(setupPanel, "SETUP");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(scoreboardPanel, "SCOREBOARD");
        mainPanel.add(replayPanel, "REPLAY");

        setContentPane(mainPanel);
        showMenu();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");
    }

    public void showSetup() {
        setupPanel.reset();
        cardLayout.show(mainPanel, "SETUP");
    }

    public void startGame(List<Player> players, int type) {
        if (type == 0) {
            gamePanel.startNewPlayerGame(players);
        }
        if(type == 1) {
            gamePanel.startNewBotGame(players);
        }
        cardLayout.show(mainPanel, "GAME");
    }

    public void showScoreboard() {
        scoreboardPanel.updateTable(scoreboard.standings());
        cardLayout.show(mainPanel, "SCOREBOARD");
    }

    public void showReplay() {
        replayPanel.refreshFileList();
        cardLayout.show(mainPanel, "REPLAY");
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
