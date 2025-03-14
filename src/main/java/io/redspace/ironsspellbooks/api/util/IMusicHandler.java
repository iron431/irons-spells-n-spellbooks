package io.redspace.ironsspellbooks.api.util;

public interface IMusicHandler {
    /**
     * Begin music, or resume music from save
     */
    void init();

    /**
     * Begin stop sequence
     */
    void stop();

    void tick();

    /**
     * Returns whether instance is completely finished playing music
     */
    boolean isDone();

    /**
     * Immediately cut all audio
     */
    void hardStop();

    /**
     * Interrupt stop sequence (if applicable) and attempt to resume playing
     */
    void triggerResume();
}
