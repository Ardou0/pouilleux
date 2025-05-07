package core.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel: adjust music/effects volume and clear replay history.
 */
public class SettingsPanel extends JPanel {
    public SettingsPanel(MainFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel(
                "Configure music and effects volume, and manage replay history."
        );
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(descLabel);

        add(Box.createVerticalStrut(30));

        // Music volume slider
        JLabel musicLabel = new JLabel("Music Volume");
        musicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(musicLabel);

        JSlider musicSlider = new JSlider(
                0, 100, parent.getSettings().getMusicVolume()
        );
        musicSlider.setMajorTickSpacing(20);
        musicSlider.setPaintTicks(true);
        musicSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicSlider.addChangeListener(e -> {
            int vol = musicSlider.getValue();
            parent.getSettings().setMusicVolume(vol);
            // immediately adjust any playing music
            SoundManager.updateMusicVolume();
        });
        add(musicSlider);

        add(Box.createVerticalStrut(20));

        // Effects volume slider
        JLabel effectsLabel = new JLabel("Effects Volume");
        effectsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(effectsLabel);

        JSlider effectsSlider = new JSlider(
                0, 100, parent.getSettings().getEffectsVolume()
        );
        effectsSlider.setMajorTickSpacing(20);
        effectsSlider.setPaintTicks(true);
        effectsSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        effectsSlider.addChangeListener(e ->
                parent.getSettings().setEffectsVolume(effectsSlider.getValue())
        );
        add(effectsSlider);

        add(Box.createVerticalStrut(30));

        // Clear replay history button
        JButton clearHistoryBtn = new SoundButton("Clear Replay History");
        clearHistoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearHistoryBtn.addActionListener(e -> {
            parent.clearReplayHistory();
            JOptionPane.showMessageDialog(
                    this, "Replay history cleared."
            );
        });
        add(clearHistoryBtn);

        // Glue to push Back button to bottom
        add(Box.createVerticalGlue());

        // Back button aligned to bottom-right
        JButton backBtn = new SoundButton("Back to Menu");
        backBtn.addActionListener(e -> parent.showMenu());
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalGlue());
        hBox.add(backBtn);
        add(hBox);
    }
}