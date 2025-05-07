package core.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel allowing the user to select a replay file and step through its contents.
 */
public class ReplayPanel extends JPanel {
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

        content = new JPanel(cards);
        add(content, BorderLayout.CENTER);

        // --- File list view ---
        JPanel listView = new JPanel(new BorderLayout(10,10));
        listView.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listView.add(new JScrollPane(fileList), BorderLayout.CENTER);
        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        openButton = new SoundButton("Open Replay");
        backMenuButton = new SoundButton("Back to Menu");
        listButtons.add(backMenuButton);
        listButtons.add(openButton);
        listView.add(listButtons, BorderLayout.SOUTH);
        content.add(listView, "list");

        // --- Replay view ---
        JPanel replayView = new JPanel(new BorderLayout(10,10));
        replayArea = new JTextArea();
        replayArea.setEditable(false);
        replayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        replayView.add(new JScrollPane(replayArea), BorderLayout.CENTER);

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevButton = new SoundButton("< Prev");
        nextButton = new SoundButton("Next >");
        backListButton = new SoundButton("Back to Replays");
        backToMenuButton = new SoundButton("Back to Menu");
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
        prevButton.addActionListener(e -> showStep(currentStep-1));
        nextButton.addActionListener(e -> showStep(currentStep+1));
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
            } catch (IOException ignored) {}
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
        nextButton.setEnabled(currentStep < steps.size()-1);
    }
}
