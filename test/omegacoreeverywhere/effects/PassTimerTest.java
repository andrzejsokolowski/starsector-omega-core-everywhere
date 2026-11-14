package omegacoreeverywhere.effects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PassTimerTest {

    private static final long MS = 1_000_000L;

    @Test
    void firstFrameRunsAPass() {
        assertTrue(new PassTimer().due(123_456_789L));
    }

    @Test
    void waitsASecondBetweenPasses() {
        PassTimer timer = new PassTimer();
        timer.ran(1000 * MS);
        assertFalse(timer.due(1000 * MS));
        assertFalse(timer.due(1999 * MS));
        assertTrue(timer.due(2000 * MS));
        assertTrue(timer.due(5000 * MS));
    }

    @Test
    void aSkippedPassStaysDue() {
        // The pass could not run: no pass ran, so the next frame tries again.
        PassTimer timer = new PassTimer();
        timer.ran(0);
        assertTrue(timer.due(1100 * MS));
        assertTrue(timer.due(1116 * MS));
        timer.ran(1116 * MS);
        assertFalse(timer.due(1132 * MS));
    }

    @Test
    void forceRunsTheNextFrame() {
        PassTimer timer = new PassTimer();
        timer.ran(0);
        timer.forceNext();
        assertTrue(timer.due(1 * MS));
        timer.ran(1 * MS);
        assertFalse(timer.due(2 * MS));
    }

    @Test
    void nanoTimeCanBeNegative() {
        // System.nanoTime() has an arbitrary origin and may be negative; only differences count.
        PassTimer timer = new PassTimer();
        timer.ran(-10_000 * MS);
        assertFalse(timer.due(-9_100 * MS));
        assertTrue(timer.due(-9_000 * MS));
    }

    @Test
    void passesPerSecondAtHighFrameRates() {
        // 120 frames a second for 10 seconds: one pass a second instead of 120.
        PassTimer timer = new PassTimer();
        int passes = 0;
        for (int frame = 0; frame < 1200; frame++) {
            long now = frame * 1_000_000_000L / 120;
            if (timer.due(now)) {
                timer.ran(now);
                passes++;
            }
        }
        assertTrue(passes >= 10 && passes <= 11, "passes: " + passes);
    }
}
