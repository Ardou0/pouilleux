package core.gui;

import java.util.prefs.Preferences;

/**
 * Holds application-wide settings (persisted between launches).
 */
public class AppSettings {
    private static final String KEY_MUSIC_VOLUME   = "musicVolume";
    private static final String KEY_EFFECTS_VOLUME = "effectsVolume";
    private static final int    DEFAULT_VOLUME     = 50;

    // Using the Preferences node for this package
    private final Preferences prefs = Preferences.userNodeForPackage(AppSettings.class);

    /** @return current music volume (0–100) */
    public int getMusicVolume() {
        return prefs.getInt(KEY_MUSIC_VOLUME, DEFAULT_VOLUME);
    }

    /** Persist a new music volume (0–100) */
    public void setMusicVolume(int volume) {
        prefs.putInt(KEY_MUSIC_VOLUME, clamp(volume));
    }

    /** @return current effects volume (0–100) */
    public int getEffectsVolume() {
        return prefs.getInt(KEY_EFFECTS_VOLUME, DEFAULT_VOLUME);
    }

    /** Persist a new effects volume (0–100) */
    public void setEffectsVolume(int volume) {
        prefs.putInt(KEY_EFFECTS_VOLUME, clamp(volume));
    }

    /** Ensures volume stays in [0,100] */
    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}
