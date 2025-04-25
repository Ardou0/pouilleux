package core.gui;

import core.model.Player;
import core.model.StrategyFactory;
import core.model.MoveStrategy;
import core.model.DrawThenPurgeStrategy;
import core.model.MixedRandomStrategy;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Panel to configure players before starting a new game.
 * Supports Solo mode, Local Shared Multiplayer, and (disabled) Internet Multiplayer.
 */
public class SetupPanel extends JPanel {
    private final MainFrame parent;

    private final JRadioButton soloButton;
    private final JRadioButton localButton;
    private final JRadioButton internetButton;
    private final JSpinner humanCountSpinner;
    private final JPanel playersConfigPanel;
    private final JButton startButton;

    public SetupPanel(MainFrame parent) {
        this.parent = Objects.requireNonNull(parent);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // Mode selection: Solo, Local, Internet (disabled)
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        soloButton = new JRadioButton("Solo");
        localButton = new JRadioButton("Local Multiplayer");
        internetButton = new JRadioButton("Internet Multiplayer");
        internetButton.setEnabled(false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(soloButton);
        modeGroup.add(localButton);
        modeGroup.add(internetButton);
        modePanel.add(new JLabel("Mode:"));
        modePanel.add(soloButton);
        modePanel.add(localButton);
        modePanel.add(internetButton);
        add(modePanel, BorderLayout.NORTH);

        // Center: dynamic player config
        playersConfigPanel = new JPanel();
        playersConfigPanel.setLayout(new BoxLayout(playersConfigPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(playersConfigPanel);
        add(scroll, BorderLayout.CENTER);

        // Bottom: controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("Start Game");
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        // Spinner for human count in local multiplayer (2-4)
        humanCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));

        // Listeners
        soloButton.addActionListener(e -> rebuildPlayerConfig());
        localButton.addActionListener(e -> rebuildPlayerConfig());
        // internetButton is disabled
        humanCountSpinner.addChangeListener(e -> rebuildPlayerConfig());
        startButton.addActionListener(e -> onStart());

        reset();
    }

    /** Reset to default: Solo mode, 1 human. */
    public void reset() {
        soloButton.setSelected(true);
        humanCountSpinner.setValue(2);
        rebuildPlayerConfig();
    }

    /** Rebuilds the player config panel based on selected mode. */
    private void rebuildPlayerConfig() {
        playersConfigPanel.removeAll();
        playersConfigPanel.add(Box.createVerticalStrut(10));

        int humans;
        boolean solo = soloButton.isSelected();
        boolean local = localButton.isSelected();

        if (solo) {
            humans = 1;
        } else if (local) {
            humans = (Integer) humanCountSpinner.getValue();
        } else {
            humans = 1; // fallback
        }

        int totalPlayers = solo ? 4 : humans; // solo fills with bots, local only humans

        // If local mode, show spinner
        if (local) {
            JPanel spinnerRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            spinnerRow.add(new JLabel("Number of human players (2-4):"));
            spinnerRow.add(humanCountSpinner);
            playersConfigPanel.add(spinnerRow);
        }

        // Create rows: humans first
        for (int i = 1; i <= humans; i++) {
            playersConfigPanel.add(makePlayerRow("Player " + i, true));
            playersConfigPanel.add(Box.createVerticalStrut(5));
        }
        // Solo mode: fill remaining with bots
        if (solo) {
            for (int j = 2; j <= 4; j++) {
                playersConfigPanel.add(makePlayerRow("Basic Bot " + (j-1), false));
                playersConfigPanel.add(Box.createVerticalStrut(5));
            }
        }

        playersConfigPanel.revalidate();
        playersConfigPanel.repaint();
    }

    /** Creates a row: name field (editable if human), strategy combo (for bots). */
    private JPanel makePlayerRow(String defaultName, boolean isHuman) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(defaultName, 12);
        nameField.setEditable(isHuman);
        row.add(new JLabel("Name:"));
        row.add(nameField);

        JComboBox<String> stratCombo = new JComboBox<>(new String[] {
                "Basic Bot", "Random Bot"
        });
        stratCombo.setEnabled(!isHuman);
        row.add(new JLabel("Strategy:"));
        row.add(stratCombo);

        // store for retrieval
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

            // <— only process rows that have our flag set
            Object humanFlag = row.getClientProperty("isHuman");
            if (!(humanFlag instanceof Boolean)) {
                // this is probably the spinner row (or some vertical strut)
                continue;
            }

            JTextField nameField = (JTextField) row.getClientProperty("nameField");
            @SuppressWarnings("unchecked")
            JComboBox<String> stratCombo = (JComboBox<String>) row.getClientProperty("stratCombo");
            boolean isHuman = (Boolean) humanFlag;
            if(!isHuman) {
                bots = 1;
            }

            String name = nameField.getText().trim();
            MoveStrategy strat = isHuman
                    ? new HumanStrategy()
                    : ("Random Bot".equals(stratCombo.getSelectedItem())
                    ? new MixedRandomStrategy()
                    : new DrawThenPurgeStrategy());

            Player p = new Player(name, List.of(), strat);
            players.add(p);
        }

        parent.startGame(players, bots);
    }
}
