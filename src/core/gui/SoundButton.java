package core.gui;

import javax.swing.*;

/**
 * JButton that plays a click sound on each press.
 */
public class SoundButton extends JButton {
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
    }
}
