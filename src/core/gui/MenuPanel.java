package core.gui;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu panel with options to start a new game,
 * view the scoreboard, replay past games, or exit.
 */
public class MenuPanel extends JPanel {
    public MenuPanel(MainFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        add(Box.createVerticalGlue());

        JButton newGameButton = new SoundButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(e -> parent.showSetup());
        add(newGameButton);
        add(Box.createVerticalStrut(20));

        JButton scoreboardButton = new SoundButton("Scoreboard");
        scoreboardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreboardButton.addActionListener(e -> parent.showScoreboard());
        add(scoreboardButton);
        add(Box.createVerticalStrut(20));

        JButton replayButton = new SoundButton("Replay");
        replayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        replayButton.addActionListener(e -> parent.showReplay());
        add(replayButton);
        add(Box.createVerticalStrut(20));

        JButton settingsButton = new SoundButton("Settings");
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.addActionListener(e -> parent.showSettings());
        add(settingsButton);
        add(Box.createVerticalStrut(20));

        JButton exitButton = new SoundButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);

        add(Box.createVerticalGlue());
    }
}
