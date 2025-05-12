package core.gui;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Manages click and background music playback, with volume control.
 */
public class SoundManager {
    private static AppSettings settings;
    private static Clip musicClip;

    /**
     * Call once at startup, before building any UI components.
     * Hooks button clicks to the pop effect.
     */
    public static void install(AppSettings appSettings) {
        settings = appSettings;
        Toolkit.getDefaultToolkit().addAWTEventListener(evt -> {
            if (!(evt instanceof ActionEvent)) return;
            Object src = ((ActionEvent) evt).getSource();
            if (src instanceof AbstractButton) {
                playClick();
            }
        }, AWTEvent.ACTION_EVENT_MASK);
    }

    /** Play the click/pop sound. Overlapping calls allowed. */
    public static void playClick() {
        playEffect("/sounds/pop.wav", "SoundManager-Click");
    }

    /** Play the negative/end game sound. Overlapping calls allowed. */
    public static void playNegative() {
        playEffect("/sounds/negative.wav", "SoundManager-Negative");
    }

    /**
     * Internal helper: loads the resource and plays it asynchronously.
     */
    private static void playEffect(String resourcePath, String threadPrefix) {
        new Thread(() -> {
            try {
                // Attempt to load via class resource
                InputStream raw = SoundManager.class.getResourceAsStream(resourcePath);
                if (raw == null) {
                    // Fallback to context class loader
                    String path = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                    raw = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
                }
                if (raw == null) {
                    System.err.println("SoundManager: resource not found: " + resourcePath);
                    return;
                }

                // Wrap in BufferedInputStream to enable mark/reset
                try (BufferedInputStream bis = new BufferedInputStream(raw);
                     AudioInputStream ais = AudioSystem.getAudioInputStream(bis)) {

                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    setVolume(clip, settings.getEffectsVolume());
                    clip.start();
                    clip.addLineListener(evt -> {
                        if (evt.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, threadPrefix + "." + System.currentTimeMillis()).start();
    }

    /**
     * Play background music (single instance). Stops any prior track.
     * Uses the "musicVolume" setting.
     */
    public static void playMusic(String resourcePath) {
        stopMusic();
        try {
            InputStream raw = SoundManager.class.getResourceAsStream(resourcePath);
            if (raw == null) {
                System.err.println("SoundManager: music resource not found: " + resourcePath);
                return;
            }
            // wrap to get mark/reset support
            try (BufferedInputStream is = new BufferedInputStream(raw);
                 AudioInputStream ais = AudioSystem.getAudioInputStream(is)) {
                musicClip = AudioSystem.getClip();
                musicClip.open(ais);
                setVolume(musicClip, settings.getMusicVolume());
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Stop the background music if playing. */
    public static void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    /**
     * Dynamically update volume of background music based on settings.
     */
    public static void updateMusicVolume() {
        if (musicClip != null && musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            setVolume(musicClip, settings.getMusicVolume());
        }
    }

    /**
     * Helper to set volume on a Clip using MASTER_GAIN.
     */
    private static void setVolume(Clip clip, int volumePercent) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB;
            if (volumePercent <= 0) dB = gain.getMinimum();
            else dB = 20f * (float) Math.log10(volumePercent / 100f);
            gain.setValue(Math.max(dB, gain.getMinimum()));
        }
    }
}
