package omegacoreeverywhere;

import lunalib.lunaSettings.LunaSettings;

/** LunaLib values, read live. A missing value falls back to the default in LunaSettings.csv. */
public final class Settings {

    static final boolean DEFAULT_DROPS_ENABLED = true;
    static final float DEFAULT_DROP_CHANCE = 0.33f;
    static final boolean DEFAULT_EFFECTS_ENABLED = true;

    private Settings() {}

    static boolean dropsEnabled() {
        Boolean value = LunaSettings.getBoolean(OmegaCoreEverywherePlugin.MOD_ID, "oce_drops_enabled");
        return value == null ? DEFAULT_DROPS_ENABLED : value;
    }

    /** Read every frame by the effects, so switching it takes hold at once. */
    public static boolean effectsEnabled() {
        Boolean value = LunaSettings.getBoolean(OmegaCoreEverywherePlugin.MOD_ID, "oce_effects_enabled");
        return value == null ? DEFAULT_EFFECTS_ENABLED : value;
    }

    static final float DEFAULT_CAPTAIN_TIMEFLOW = 25f;

    /** Percent faster timeflow for a ship captained by one of the player's Omega cores. 0 is off. */
    public static float captainTimeflow() {
        Double value = LunaSettings.getDouble(OmegaCoreEverywherePlugin.MOD_ID, "oce_captain_timeflow");
        if (value == null || value.isNaN()) return DEFAULT_CAPTAIN_TIMEFLOW;
        return (float) Math.max(0d, Math.min(200d, value));
    }

    static float dropChance() {
        Double value = LunaSettings.getDouble(OmegaCoreEverywherePlugin.MOD_ID, "oce_drop_chance");
        if (value == null || value.isNaN()) return DEFAULT_DROP_CHANCE;
        return (float) Math.max(0d, Math.min(1d, value));
    }
}
