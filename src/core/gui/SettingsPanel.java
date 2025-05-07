package core.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings panel: adjust music/effects volume and clear replay history.
 */
public class SettingsPanel extends JPanel {
    private static final Color PANEL_BG      = new Color(0xFE, 0xF5, 0xD7); // #FEF5D7
    private static final Color OUTER_BORDER = new Color(0x14, 0x1A, 0x2B); // #141A2B
    private static final Color INNER_BORDER = new Color(0xDA, 0x4D, 0x4C); // #DA4D4C
    private static final Color BUTTON_BG     = PANEL_BG;
    private static final Color BUTTON_FG     = OUTER_BORDER;

    public SettingsPanel(MainFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(PANEL_BG);
        setOpaque(true);

        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BUTTON_FG);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel(
                "Configure music and effects volume, and manage replay history."
        );
        descLabel.setForeground(BUTTON_FG);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(descLabel);

        add(Box.createVerticalStrut(30));

        // Music volume slider
        JLabel musicLabel = new JLabel("Music Volume");
        musicLabel.setForeground(BUTTON_FG);
        musicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(musicLabel);

        JSlider musicSlider = new JSlider(0, 100, parent.getSettings().getMusicVolume());
        styleSlider(musicSlider);
        musicSlider.addChangeListener(e -> {
            int vol = musicSlider.getValue();
            parent.getSettings().setMusicVolume(vol);
            SoundManager.updateMusicVolume();
        });
        add(musicSlider);

        add(Box.createVerticalStrut(20));

        // Effects volume slider
        JLabel effectsLabel = new JLabel("Effects Volume");
        effectsLabel.setForeground(BUTTON_FG);
        effectsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(effectsLabel);

        JSlider effectsSlider = new JSlider(0, 100, parent.getSettings().getEffectsVolume());
        styleSlider(effectsSlider);
        effectsSlider.addChangeListener(e ->
                parent.getSettings().setEffectsVolume(effectsSlider.getValue())
        );
        add(effectsSlider);

        add(Box.createVerticalStrut(30));

        // Clear replay history button
        JButton clearHistoryBtn = new SoundButton("Clear Replay History");
        styleButton(clearHistoryBtn);
        clearHistoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearHistoryBtn.addActionListener(e -> {
            parent.clearReplayHistory();
            JOptionPane.showMessageDialog(
                    this, "Replay history cleared.",
                    "Info", JOptionPane.INFORMATION_MESSAGE
            );
        });
        add(clearHistoryBtn);

        // Glue to push Back button to bottom
        add(Box.createVerticalGlue());

        // Back button aligned to bottom-right
        JButton backBtn = new SoundButton("Back to Menu");
        styleButton(backBtn);
        backBtn.addActionListener(e -> parent.showMenu());
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalGlue());
        hBox.add(backBtn);
        add(hBox);
    }

    /**
     * Styles a JButton with the project color scheme and dual border.
     */
    private void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTER_BORDER, 2),
                BorderFactory.createLineBorder(INNER_BORDER, 2)
        ));
    }

    /**
     * Styles a JSlider to match the project theme.
     */
    private void styleSlider(JSlider slider) {
        slider.setMajorTickSpacing(20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(PANEL_BG);
        slider.setForeground(BUTTON_FG);
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
