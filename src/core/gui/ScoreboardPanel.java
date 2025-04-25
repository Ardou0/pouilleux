package core.gui;

import core.persistence.ScoreEntry;
import core.persistence.Scoreboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel to display the cumulative loss scoreboard in a JTable.
 */
public class ScoreboardPanel extends JPanel {
    private final MainFrame parent;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton backButton;

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

        // Back button
        backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> parent.showMenu());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Update the JTable with a fresh list of ScoreEntry.
     */
    public void updateTable(List<ScoreEntry> entries) {
        tableModel.setRowCount(0);
        for (ScoreEntry entry : entries) {
            tableModel.addRow(new Object[]{entry.name(), entry.losses()});
        }
    }
}
