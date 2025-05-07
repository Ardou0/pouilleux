package core.gui;

import core.model.*;
import core.persistence.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

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
    private SettingsPanel settingsPanel;

    private Scoreboard scoreboard;

    public MainFrame() {
        super("Pouilleux");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        scoreboard = new Scoreboard();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(23, 130, 189));

        // initialize panels
        menuPanel = new MenuPanel(this);
        menuPanel.setBackground(new Color(23, 130, 189));
        setupPanel = new SetupPanel(this);
        setupPanel.setBackground(new Color(23, 130, 189));
        gamePanel = new GamePanel(this);
        gamePanel.setBackground(new Color(23, 130, 189));
        scoreboardPanel = new ScoreboardPanel(this);
        scoreboardPanel.setBackground(new Color(23, 130, 189));
        replayPanel = new ReplayPanel(this);
        replayPanel.setBackground(new Color(23, 130, 189));
        settingsPanel = new SettingsPanel(this);
        settingsPanel.setBackground(new Color(23, 130, 189));

        // add to main panel
        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(setupPanel, "SETUP");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(scoreboardPanel, "SCOREBOARD");
        mainPanel.add(replayPanel, "REPLAY");
        mainPanel.add(settingsPanel, "SETTINGS");

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
        if (type == 1) {
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

    public void showSettings() {
        cardLayout.show(mainPanel, "SETTINGS");
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public AppSettings getSettings()   {
        AppSettings settings = new AppSettings();
        return settings;
    }

    public void clearReplayHistory() {
        ReplayLogger.clearAll();
        replayPanel.refreshFileList();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            try (InputStream in = MainFrame.class.getResourceAsStream("/images/icon.png")) {
                if (in != null) {
                    Image img = ImageIO.read(in);
                    frame.setIconImage(img);
                } else {
                    System.err.println("⚠️ /images/icon.png introuvable !");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            SoundManager.install(frame.getSettings());
            frame.setVisible(true);
            SoundManager.playMusic("/sounds/ambiance.wav");
        });
    }
}
