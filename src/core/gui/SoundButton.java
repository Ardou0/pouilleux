package core.gui;

import javax.swing.*;
import java.awt.*;

/**
 * JButton that plays a click sound on each press.
 */
public class SoundButton extends JButton {


    private static final Color OUTER_BORDER_COLOR = new Color(0x14, 0x1A, 0x2B); // #141A2B
    private static final Color INNER_BORDER_COLOR = new Color(0xDA, 0x4D, 0x4C); // #DA4D4C
    private static final Color BUTTON_BG = new Color(0xFE, 0xF5, 0xD7); // #FEF5D7
    private static final Color BUTTON_FG = new Color(0x14, 0x1A, 0x2B); // #141A2B

    public SoundButton(String text) {
        super(text);
        init();
    }

    public SoundButton(Icon icon) {
        super(icon);
        init();
    }

    public SoundButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    private void init() {
        // Hook ClickSound directly
        addActionListener(e -> SoundManager.playClick());
        styleButton(this);
    }

    private void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTER_BORDER_COLOR, 3),
                BorderFactory.createLineBorder(INNER_BORDER_COLOR, 3)
        ));
    }
}
