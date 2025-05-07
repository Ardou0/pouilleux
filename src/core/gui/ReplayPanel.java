package core.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel allowing the user to select a replay file and step through its contents.
 */
public class ReplayPanel extends JPanel {
    private static final Color PANEL_BG       = new Color(0xFE, 0xF5, 0xD7); // #FEF5D7
    private static final Color OUTER_BORDER  = new Color(0x14, 0x1A, 0x2B); // #141A2B
    private static final Color INNER_BORDER  = new Color(0xDA, 0x4D, 0x4C); // #DA4D4C
    private static final Color BUTTON_BG     = PANEL_BG;
    private static final Color BUTTON_FG     = OUTER_BORDER;

    private final MainFrame parent;
    private final CardLayout cards = new CardLayout();
    private final JPanel content;

    // List view
    private final JList<String> fileList;
    private final DefaultListModel<String> listModel;
    private final JButton openButton;
    private final JButton backMenuButton;

    // Replay view
    private final JTextArea replayArea;
    private final JButton prevButton, nextButton;
    private final JButton backListButton, backToMenuButton;
    private List<List<String>> steps = new ArrayList<>();
    private int currentStep = 0;

    public ReplayPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        content = new JPanel(cards);
        content.setBackground(PANEL_BG);
        add(content, BorderLayout.CENTER);

        // --- File list view ---
        JPanel listView = new JPanel(new BorderLayout(10, 10));
        listView.setBackground(PANEL_BG);
        listView.setBorder(new EmptyBorder(10, 10, 10, 10));

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setBackground(BUTTON_BG);
        fileList.setForeground(BUTTON_FG);
        listView.add(new JScrollPane(fileList), BorderLayout.CENTER);

        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listButtons.setBackground(PANEL_BG);
        openButton = new SoundButton("Open Replay");
        backMenuButton = new SoundButton("Back to Menu");
        styleButton(openButton);
        styleButton(backMenuButton);
        listButtons.add(backMenuButton);
        listButtons.add(openButton);
        listView.add(listButtons, BorderLayout.SOUTH);

        content.add(listView, "list");

        // --- Replay view ---
        JPanel replayView = new JPanel(new BorderLayout(10, 10));
        replayView.setBackground(PANEL_BG);
        replayView.setBorder(new EmptyBorder(10, 10, 10, 10));

        replayArea = new JTextArea();
        replayArea.setEditable(false);
        replayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        replayArea.setBackground(BUTTON_BG);
        replayArea.setForeground(BUTTON_FG);
        replayArea.setBorder(BorderFactory.createLineBorder(OUTER_BORDER, 2));
        replayView.add(new JScrollPane(replayArea), BorderLayout.CENTER);

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlBar.setBackground(PANEL_BG);
        prevButton = new SoundButton("< Prev");
        nextButton = new SoundButton("Next >");
        backListButton = new SoundButton("Back to Replays");
        backToMenuButton = new SoundButton("Back to Menu");
        styleButton(prevButton);
        styleButton(nextButton);
        styleButton(backListButton);
        styleButton(backToMenuButton);
        controlBar.add(prevButton);
        controlBar.add(nextButton);
        controlBar.add(backListButton);
        controlBar.add(backToMenuButton);
        replayView.add(controlBar, BorderLayout.SOUTH);

        content.add(replayView, "replay");

        // Load list
        refreshFileList();

        // Listeners
        openButton.addActionListener(this::onOpen);
        backMenuButton.addActionListener(e -> parent.showMenu());
        backListButton.addActionListener(e -> cards.show(content, "list"));
        backToMenuButton.addActionListener(e -> parent.showMenu());
        prevButton.addActionListener(e -> showStep(currentStep - 1));
        nextButton.addActionListener(e -> showStep(currentStep + 1));
    }

    /** Reloads the replay directory into the list. */
    public void refreshFileList() {
        listModel.clear();
        Path dir = Paths.get("replays");
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try {
                Files.list(dir)
                        .filter(p -> p.toString().endsWith(".log"))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .sorted()
                        .forEach(listModel::addElement);
            } catch (IOException ignored) {
            }
        }
    }

    /** Handles "Open Replay" click: parse file into steps. */
    private void onOpen(ActionEvent e) {
        String fileName = fileList.getSelectedValue();
        if (fileName == null) return;
        Path file = Paths.get("replays", fileName);
        try {
            List<String> lines = Files.readAllLines(file);
            parseSteps(lines);
            showStep(0);
            cards.show(content, "replay");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not read replay file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Splits raw lines into individual step blocks. */
    private void parseSteps(List<String> lines) {
        steps.clear();
        List<String> block = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("STEP ") && !block.isEmpty()) {
                steps.add(new ArrayList<>(block));
                block.clear();
            }
            block.add(line);
        }
        if (!block.isEmpty()) steps.add(block);
        currentStep = 0;
    }

    /** Display a given step index. */
    private void showStep(int index) {
        if (index < 0 || index >= steps.size()) return;
        currentStep = index;
        List<String> lines = steps.get(index);
        replayArea.setText(String.join("\n", lines));
        replayArea.setCaretPosition(0);
        prevButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < steps.size() - 1);
    }

    /** Styles buttons with the project's color scheme. */
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
