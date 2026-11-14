package omegacoreeverywhere.effects;

/**
 * Decides when {@link OmegaEffects} runs its next pass over the colonies. It counts real time, so it
 * also runs while the game is paused (the colony screen is open then), and fast-forward does not
 * speed it up.
 *
 * Once a second is enough: every effect is a modifier or a hold in the {@link oddiszcommons.ledger.Ledger},
 * which stays in place between passes. A pass only has to notice what changed (a core put in or taken
 * out, a building lost, a setting switched). Cores are only put in while the game is paused, and
 * {@link OmegaEffects} also runs a pass on the first frame the game runs again, so nothing new waits
 * for game time to move. The colony screen shows a change at most a second late.
 *
 * No game classes, so it is tested in {@code test/}.
 */
final class PassTimer {

    /** Seconds of real time between passes. */
    static final double INTERVAL_SECONDS = 1.0;

    private final long intervalNanos;
    private long last;
    private boolean force = true;

    PassTimer() {
        this(INTERVAL_SECONDS);
    }

    PassTimer(double intervalSeconds) {
        intervalNanos = (long) (intervalSeconds * 1e9);
    }

    /** Whether a pass should run at {@code nowNanos} (from {@link System#nanoTime()}). */
    boolean due(long nowNanos) {
        return force || nowNanos - last >= intervalNanos;
    }

    /** A pass ran at {@code nowNanos}: the next one is due a full interval later. */
    void ran(long nowNanos) {
        last = nowNanos;
        force = false;
    }

    /** The next frame runs a pass whatever the time. */
    void forceNext() {
        force = true;
    }
}
