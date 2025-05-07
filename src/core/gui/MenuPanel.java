package core.gui;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu panel with options to start a new game,
 * view the scoreboard, replay past games, or exit.
 */
public class MenuPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(200, 40);

    public MenuPanel(MainFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Title
        JLabel titleLabel = new JLabel("Pouilleux !");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 48f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        // Extra space between title and description
        add(Box.createVerticalStrut(20));

        // Description subtitle
        JLabel subtitleLabel = new JLabel(
                "Java application allowing you to play Pouilleux, a variation of the card game “Old Maid”, with a user-friendly graphical interface."
        );
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(subtitleLabel);

        // Reduced space between description and menu options
        add(Box.createVerticalStrut(10));
        add(Box.createVerticalGlue());

        // Standard buttons with uniform style and size
        JButton newGameButton = createButton("New Game");
        newGameButton.addActionListener(e -> parent.showSetup());
        add(newGameButton);
        add(Box.createVerticalStrut(20));

        JButton scoreboardButton = createButton("Scoreboard");
        scoreboardButton.addActionListener(e -> parent.showScoreboard());
        add(scoreboardButton);
        add(Box.createVerticalStrut(20));

        JButton replayButton = createButton("Replay");
        replayButton.addActionListener(e -> parent.showReplay());
        add(replayButton);
        add(Box.createVerticalStrut(20));

        JButton settingsButton = createButton("Settings");
        settingsButton.addActionListener(e -> parent.showSettings());
        add(settingsButton);
        add(Box.createVerticalStrut(20));

        // Exit button styled with same colors but default size
        JButton exitButton = new SoundButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension EXIT_BUTTON_SIZE = new Dimension(100, 40);
        exitButton.setPreferredSize(EXIT_BUTTON_SIZE);
        exitButton.setMaximumSize(EXIT_BUTTON_SIZE);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);

        add(Box.createVerticalGlue());
    }

    /**
     * Creates and styles a SoundButton with uniform size, colors, and border.
     */
    private JButton createButton(String text) {
        JButton button = new SoundButton(text);
        button.setPreferredSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

}
