package core.gui;

import core.model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Panel to configure players before starting a new game.
 * Supports Solo mode, Local Shared Multiplayer, and (disabled) Internet Multiplayer.
 */
public class SetupPanel extends JPanel {
    private static final Color PANEL_BG      = new Color(0xFE, 0xF5, 0xD7); // #FEF5D7
    private static final Color OUTER_BORDER = new Color(0x14, 0x1A, 0x2B); // #141A2B
    private static final Color INNER_BORDER = new Color(0xDA, 0x4D, 0x4C); // #DA4D4C
    private static final Color BUTTON_BG     = PANEL_BG;
    private static final Color BUTTON_FG     = OUTER_BORDER;
    private static final Color TEXT_FG       = OUTER_BORDER;
    private static final Color CONTROL_BG    = PANEL_BG;

    private final MainFrame parent;
    private final JRadioButton soloButton;
    private final JRadioButton localButton;
    private final JRadioButton internetButton;
    private final JSpinner humanCountSpinner;
    private final JPanel playersConfigPanel;
    private final JButton startButton;
    private final JButton backButton;

    public SetupPanel(MainFrame parent) {
        this.parent = Objects.requireNonNull(parent);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(PANEL_BG);
        setOpaque(true);

        // Mode selection: Solo, Local, Internet (disabled)
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.setBackground(PANEL_BG);
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setForeground(TEXT_FG);
        modePanel.add(modeLabel);
        soloButton = new JRadioButton("Solo");
        localButton = new JRadioButton("Local Multiplayer");
        internetButton = new JRadioButton("Internet Multiplayer");
        internetButton.setEnabled(false);
        styleRadio(soloButton);
        styleRadio(localButton);
        styleRadio(internetButton);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(soloButton);
        modeGroup.add(localButton);
        modeGroup.add(internetButton);
        modePanel.add(soloButton);
        modePanel.add(localButton);
        modePanel.add(internetButton);
        add(modePanel, BorderLayout.NORTH);

        // Center: dynamic player config
        playersConfigPanel = new JPanel();
        playersConfigPanel.setBackground(PANEL_BG);
        playersConfigPanel.setLayout(new BoxLayout(playersConfigPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(playersConfigPanel);
        scroll.getViewport().setBackground(PANEL_BG);
        add(scroll, BorderLayout.CENTER);

        // Bottom: controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(PANEL_BG);
        backButton = new SoundButton("Back to Menu");
        startButton = new SoundButton("Start Game");
        styleButton(backButton);
        styleButton(startButton);
        bottom.add(backButton);
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        // Spinner for human count in local multiplayer (2-4)
        humanCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
        styleSpinner(humanCountSpinner);

        // Listeners
        soloButton.addActionListener(e -> rebuildPlayerConfig());
        localButton.addActionListener(e -> rebuildPlayerConfig());
        humanCountSpinner.addChangeListener(e -> rebuildPlayerConfig());
        startButton.addActionListener(e -> onStart());
        backButton.addActionListener(e -> parent.showMenu());

        reset();
    }

    /** Reset to default: Solo mode, 2 players in local. */
    public void reset() {
        soloButton.setSelected(true);
        humanCountSpinner.setValue(2);
        rebuildPlayerConfig();
    }

    /** Rebuilds the player config panel based on selected mode. */
    private void rebuildPlayerConfig() {
        playersConfigPanel.removeAll();
        playersConfigPanel.add(Box.createVerticalStrut(10));

        boolean solo = soloButton.isSelected();
        boolean local = localButton.isSelected();
        int humans = solo ? 1 : (Integer) humanCountSpinner.getValue();
        int totalPlayers = solo ? 4 : humans;

        // If local mode, show spinner
        if (local) {
            JPanel spinnerRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            spinnerRow.setBackground(PANEL_BG);
            JLabel label = new JLabel("Number of human players (2-4):");
            label.setForeground(TEXT_FG);
            spinnerRow.add(label);
            spinnerRow.add(humanCountSpinner);
            playersConfigPanel.add(spinnerRow);
            playersConfigPanel.add(Box.createVerticalStrut(10));
        }

        // Create rows: humans first
        for (int i = 1; i <= humans; i++) {
            JPanel row = makePlayerRow("Player " + i, true);
            playersConfigPanel.add(row);
            playersConfigPanel.add(Box.createVerticalStrut(5));
        }
        // Solo mode: fill remaining with bots
        if (solo) {
            for (int j = humans + 1; j <= 4; j++) {
                JPanel row = makePlayerRow("Basic Bot " + (j - 1), false);
                playersConfigPanel.add(row);
                playersConfigPanel.add(Box.createVerticalStrut(5));
            }
        }

        playersConfigPanel.revalidate();
        playersConfigPanel.repaint();
    }

    /** Creates a row: name field (editable if human), strategy combo (for bots). */
    private JPanel makePlayerRow(String defaultName, boolean isHuman) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(PANEL_BG);
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(TEXT_FG);
        row.add(nameLabel);
        JTextField nameField = new JTextField(defaultName, 12);
        nameField.setEditable(isHuman);
        nameField.setForeground(TEXT_FG);
        nameField.setBackground(PANEL_BG);
        nameField.setBorder(BorderFactory.createLineBorder(OUTER_BORDER, 1));
        row.add(nameField);

        JLabel stratLabel = new JLabel("Strategy:");
        stratLabel.setForeground(TEXT_FG);
        row.add(stratLabel);
        JComboBox<String> stratCombo = new JComboBox<>(new String[]{"Basic Bot", "Random Bot"});
        stratCombo.setEnabled(!isHuman);
        styleCombo(stratCombo);
        row.add(stratCombo);

        row.putClientProperty("nameField", nameField);
        row.putClientProperty("stratCombo", stratCombo);
        row.putClientProperty("isHuman", isHuman);
        return row;
    }

    /** Called when Start Game pressed. Builds Player list and invokes parent. */
    private void onStart() {
        Component[] rows = playersConfigPanel.getComponents();
        List<Player> players = new ArrayList<>();
        int bots = 0;
        for (Component comp : rows) {
            if (!(comp instanceof JPanel)) continue;
            JPanel row = (JPanel) comp;
            Object humanFlag = row.getClientProperty("isHuman");
            if (!(humanFlag instanceof Boolean)) continue;
            JTextField nameField = (JTextField) row.getClientProperty("nameField");
            @SuppressWarnings("unchecked")
            JComboBox<String> stratCombo = (JComboBox<String>) row.getClientProperty("stratCombo");
            boolean isHuman = (Boolean) humanFlag;
            if (!isHuman) bots = 1;
            String name = nameField.getText().trim();
            MoveStrategy strat = isHuman
                    ? new HumanStrategy()
                    : ("Random Bot".equals(stratCombo.getSelectedItem())
                    ? new MixedRandomStrategy()
                    : new DrawThenPurgeStrategy());
            players.add(new Player(name, List.of(), strat));
        }
        parent.startGame(players, bots);
    }

    // -- Styling helpers --
    private void styleButton(JButton b) {
        b.setBackground(BUTTON_BG);
        b.setForeground(BUTTON_FG);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTER_BORDER, 2),
                BorderFactory.createLineBorder(INNER_BORDER, 2)
        ));
    }
    private void styleRadio(JRadioButton r) {
        r.setBackground(PANEL_BG);
        r.setForeground(TEXT_FG);
        r.setOpaque(true);
    }
    private void styleSpinner(JSpinner s) {
        s.setBackground(CONTROL_BG);
        JComponent comp = s.getEditor();
        comp.setBackground(CONTROL_BG);
        s.setForeground(TEXT_FG);
        s.setOpaque(true);
    }
    private void styleCombo(JComboBox<String> c) {
        c.setBackground(CONTROL_BG);
        c.setForeground(TEXT_FG);
        c.setBorder(BorderFactory.createLineBorder(OUTER_BORDER, 1));
    }
}
