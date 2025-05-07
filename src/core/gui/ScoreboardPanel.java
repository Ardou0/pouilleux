package core.gui;

import core.persistence.ScoreEntry;
import core.persistence.Scoreboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel to display the cumulative loss scoreboard in a JTable.
 */
public class ScoreboardPanel extends JPanel {
    private static final Color PANEL_BG      = new Color(0xFE, 0xF5, 0xD7); // #FEF5D7
    private static final Color OUTER_BORDER = new Color(0x14, 0x1A, 0x2B); // #141A2B
    private static final Color INNER_BORDER = new Color(0xDA, 0x4D, 0x4C); // #DA4D4C
    private static final Color BUTTON_BG     = PANEL_BG;
    private static final Color BUTTON_FG     = OUTER_BORDER;

    private final MainFrame parent;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton backButton;
    private final JButton clearButton;

    public ScoreboardPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(PANEL_BG);
        setOpaque(true);

        // Table setup
        String[] columns = {"Player", "Losses"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setBackground(BUTTON_BG);
        table.setForeground(BUTTON_FG);
        table.getTableHeader().setBackground(BUTTON_BG);
        table.getTableHeader().setForeground(BUTTON_FG);
        table.setBorder(BorderFactory.createLineBorder(OUTER_BORDER, 2));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(PANEL_BG);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        backButton = new SoundButton("Back to Menu");
        clearButton = new SoundButton("Clear Scoreboard");
        styleButton(backButton);
        styleButton(clearButton);
        backButton.addActionListener(e -> parent.showMenu());
        clearButton.addActionListener(e -> clearScoreboard());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(PANEL_BG);
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
            tableModel.addRow(new Object[]{entry.name(), entry.losses()});
        }
    }

    /**
     * Clears both the underlying Scoreboard and the table UI immediately.
     */
    private void clearScoreboard() {
        Scoreboard scoreboard = parent.getScoreboard();
        scoreboard.clear();
        tableModel.setRowCount(0);
        List<ScoreEntry> now = scoreboard.standings();
        if (!now.isEmpty()) {
            updateTable(now);
        }
        table.revalidate();
        table.repaint();
    }

    /**
     * Applies the project color scheme and border to a button.
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
}
