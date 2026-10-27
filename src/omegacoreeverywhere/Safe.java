package omegacoreeverywhere;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;

/**
 * Every place the game calls into this mod goes through here. A crash is the worst thing this mod
 * can do (Andrzej: rather no effect than a crash), so an error in any of them is written to
 * starsector.log once per place and that call does nothing, or answers with a harmless fallback.
 *
 * Only errors the Java machine itself cannot survive, such as running out of memory, are passed on.
 */
public final class Safe {

    private static final Logger log = Global.getLogger(Safe.class);
    private static final Set<String> reported = ConcurrentHashMap.newKeySet();

    private Safe() {}

    public static void run(String where, Runnable body) {
        try {
            body.run();
        } catch (Throwable t) {
            failed(where, t);
        }
    }

    public static <T> T get(String where, Supplier<T> body, T fallback) {
        try {
            T value = body.get();
            return value == null ? fallback : value;
        } catch (Throwable t) {
            failed(where, t);
            return fallback;
        }
    }

    /** Logs the error once for this place. Passes on only what the Java machine cannot survive. */
    public static void failed(String where, Throwable t) {
        if (t instanceof VirtualMachineError && !(t instanceof StackOverflowError)) throw (VirtualMachineError) t;
        if (reported.add(where)) {
            log.error("Omega Core Everywhere: " + where + " failed; it does nothing instead of crashing the game", t);
        }
    }
}
