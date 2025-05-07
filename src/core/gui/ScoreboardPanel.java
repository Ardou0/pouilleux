package core.gui;

import core.persistence.ScoreEntry;
import core.persistence.Scoreboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Panel to display the cumulative loss scoreboard in a JTable.
 */
public class ScoreboardPanel extends JPanel {
    private final MainFrame parent;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton backButton;
    private final JButton clearButton;

    public ScoreboardPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // Table setup
        String[] columns = {"Player", "Losses"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Back + Clear button
        backButton  = new SoundButton("Back to Menu");
        clearButton = new SoundButton("Clear Scoreboard");
        backButton.addActionListener(e -> parent.showMenu());
        clearButton.addActionListener(e -> clearButtonScoreBoard());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(backButton);
        bottom.add(clearButton);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Update the JTable with a fresh list of ScoreEntry.
     */
    public void updateTable(List<ScoreEntry> entries) {
        tableModel.setRowCount(0);
        for (ScoreEntry entry : entries) {
            tableModel.addRow(new Object[]{ entry.name(), entry.losses() });
        }
    }

    /**
     * Clears both the underlying Scoreboard and the table UI immediately.
     */
    private void clearButtonScoreBoard() {
        // 1) Clear the data
        Scoreboard scoreboard = parent.getScoreboard();
        scoreboard.clear();

        // 2) Wipe out the table model right away
        tableModel.setRowCount(0);

        // 3) (Optional) If you want to reflect whatever standings() now returns:
        List<ScoreEntry> now = scoreboard.standings();
        if (!now.isEmpty()) {
            // repopulate with fresh data—probably redundant if clear() worked
            updateTable(now);
        }

        // 4) Refresh UI
        table.revalidate();
        table.repaint();
    }
}
